import Shared.Printer;
import Shared.Protocol;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerThread extends Thread {
    //Socket to communicate with the client
    protected static Socket socket;
    //Output stream to write messages to the client
    private PrintWriter writer;
    //Input stream to read message from the client
    private BufferedReader reader;
    private Server server;
    private String username;
    //Flag to indicate if a PONG message has been received from the client
    private static boolean pingPongFlag;
    private Thread pingPongThread;


    public ServerThread(Socket clientSocket, Server server) {
        try {
            this.server = server;
            this.socket = clientSocket;
            // Create the output and input streams for the ServerThread
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Set the username of the client to null initially
            username = null;
            // Set the pingPongFlag to false initially
            pingPongFlag = false;
            //Send welcome message and read message from client
            writer.println(Protocol.INITIALISE + " Welcome to the server!");
            writer.flush();

            //Start a new thread to handle the ping-pong(heartbeat) mechanism for the client
            pingPongThread = new Thread(new pingPong(this));
            pingPongThread.start();
        }catch (Exception e){
            Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);
            server.users.remove(this);
            pingPongThread.stop();
            this.stop();
        }
    }

    public void run(){
        try {
            // Listen for incoming messages in an infinite loop
            while(true){
                //Read message from client
                String messageFull = reader.readLine();
                System.out.println(messageFull);
                if(messageFull==null){continue;}
                var msg = messageFull.split(" ");

                // Determine which command the client is trying to use
                switch(msg[0]){
                    case Protocol.PONG -> {
                        pingPongFlag = true;
                    }
                    case Protocol.IDENTIFY -> {
                        //If the user is new
                        if(username == null) {
                            createUser(msg);
                        }else{
                            //already logged in
                            writer.println(Protocol.FAIL04);
                        }
                    }
                    case Protocol.BROADCAST -> {
                        if(username!=null) {
                            broadcastMessage(String.join(" ", msg));
                        }else{
                            writer.println(Protocol.FAIL03);
                        }
                    }
                    case Protocol.QUIT -> {
                        // Client is trying to disconnect
                        writer.println(Protocol.OK + " " + Protocol.GOODBYE);
                        broadcastMessage(Protocol.LEFT + " " + username);
                        server.users.remove(this);
                        writer.close();
                        reader.close();
                        //socket.close();
                        pingPongThread.stop();
                        this.stop();
                    }
                    case Protocol.USERS -> {
                        //Client has requested a list of connected users
                        userList();
                    }
                    case Protocol.PRIVATE_MSG -> {
                        // Client is trying to send a private message
                        privateMsg(String.join(" ", Arrays.copyOfRange(msg, 2, msg.length)) ,msg[1]);
                    }
                    case Protocol.SEND_PUBLIC_KEY -> {
                        // Broadcast the public key to all users
                        broadcastMessage(messageFull);
                    }
                    case Protocol.SEND_SESSION_KEY -> {
                        // Share session key with other client
                        shareSessionKey(messageFull);
                    }
                    case Protocol.OK -> {

                    }
                    default -> {
                        //Unknown command
                        writer.println(Protocol.FAIL00);
                    }
                }
                writer.flush();
            }
        }catch (Exception e){
            // Print error message and remove user from server's user list
            Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);
            server.users.remove(this);
            pingPongThread.stop();
            this.stop();
        }
    }

    //Broadcast to all clients except the sender of the message
    private void broadcastMessage(String msg){
        for(int i=0; i < server.users.size(); i++){
            //Broadcast to all clients except the sender of the message
            if(msg.startsWith(Protocol.BROADCAST) && server.users.get(i)!=this){
                // Reformat the string, insert the username of the sender into the message
                server.users.get(i).writer.println(new StringBuilder(msg).insert(Protocol.BROADCAST.length(), " " + this.username).toString());
                // Check if the message starts with Protocol.JOINED and the current client is not the sender of the message
            }else if(msg.startsWith(Protocol.JOINED) && server.users.get(i)!=this) {
                server.users.get(i).writer.println(msg);
            }else if(msg.startsWith(Protocol.BROADCAST) && server.users.get(i)==this){
                // Confirmation response sent to the client who sent this message
                server.users.get(i).writer.println(Protocol.OK + " " + msg);
            }else if(msg.startsWith(Protocol.LEFT) && server.users.get(i)!=this){
                server.users.get(i).writer.println(msg);
            }else if(msg.startsWith(Protocol.SEND_PUBLIC_KEY) && server.users.get(i)!=this){
                //Reformat string, insert the username of the sender
                String[] msgArr = msg.split(" ");
                server.users.get(i).writer.println(Protocol.SEND_PUBLIC_KEY + " " + this.username + " " + msgArr[1]);
            }
        }
    }

    //Sends a list of current users to the client that requested it
    private void userList(){
        // Initialize an empty string to store the list of users
        String userList = "";
        // Counter to keep track of the number of logged-in users
        int loggedInUsers = 0;
        for(int i=0; i < server.users.size(); i++) {
            // Check if the user has a username (i.e. is logged in)
            if(server.users.get(i).username!=null) {
                // Add the user's username to the list of users
                userList=userList.concat(server.users.get(i).username + ":");
                loggedInUsers++;
            }
        }
        if(loggedInUsers==1){
            writer.println(Protocol.FAIL06);
        }else if(loggedInUsers>0){
            // Send the list of users to the client
            writer.println(Protocol.OK + " " + Protocol.USERS + " " + userList.substring(0, userList.length() - 1));
            // Remove the last unnecessary colon (:) from the list of users
            System.out.println(Protocol.OK + " " + Protocol.USERS + " " + userList.substring(0, userList.length() - 1));
        }
    }

    //Sends a private message to the specified user
    private void privateMsg(String msg, String user){
        //Flag to track if the target user exists
        boolean userExists = false;
        //Iterate over the list of all connected clients
        for(int i=0; i < server.users.size(); i++) {
            if(server.users.get(i).username!=null && server.users.get(i).username.equals(user)) {
                writer.println(Protocol.OK + " " + Protocol.PRIVATE_MSG + " " + user + " " + msg); //Confirm/approve the command
                writer.flush();
                server.users.get(i).writer.println(Protocol.RECEIVE_PRIVATE_MSG + " " + username + " " + msg); //Send the message to the destined receiver
                userExists=true;
                break;
            }
        }
        if(!userExists){
            //If userExists is false, target user not found
            writer.println(Protocol.FAIL07);
        }
    }

    private void shareSessionKey(String msg){
        String[] msgArr = msg.split(" ");

        for(int i=0; i < server.users.size(); i++) {
            if (server.users.get(i).username != null && server.users.get(i).username.equals(msgArr[1])) {
                server.users.get(i).writer.println(Protocol.SEND_SESSION_KEY + " " + this.username + " " + msgArr[2]);
                break;
            }
        }
    }

    //Sets the correct username for the user, i.e. creates a new user
    private void createUser(String[] msgArr){
        // Check if the user is already logged in
        for(int i=0; i < server.users.size(); i++){
            if(server.users.get(i)!=this){// check current user with all other users
                if(server.users.get(i).username!=null && server.users.get(i).username.equals(msgArr[1])){
                    // If the user is already logged in, send failure message
                    writer.println(Protocol.FAIL01);
                    return;
                }
            }
        }

        //Invalid Format, contains special characters, only letters, numbers and underscore allowed
        Pattern special = Pattern.compile ("[!@#$%&*()+=|<>?{};:,<.>/\\[\\]~-]");
        Matcher hasSpecial = special.matcher(msgArr[1]);
        if(msgArr[1].length()>14 || msgArr[1].length()<3 || hasSpecial.find()){
            // If the format is invalid, send failure message
            writer.println(Protocol.FAIL02);
            return;
        }

        // Set the username for the client
        username = msgArr[1];
        writer.println(Protocol.OK + " " + Protocol.IDENTIFY + " " +  username);
        // Broadcast message to all clients that the user has joined the chat
        broadcastMessage("JOINED " + username);
    }

    private class pingPong implements Runnable{
        ServerThread parent;

        pingPong(ServerThread parent){
            this.parent=parent;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    parent.writer.println(Protocol.PING);
                    writer.flush();
                    // Sleep for 5 seconds
                    Thread.sleep(5000);

                    if(!pingPongFlag){
                        parent.writer.println(Protocol.FAIL05);
                        parent.writer.println(Protocol.DISCONNECT + " Pong timeout");


                        //Broadcast that the user has left/been disconnected from the server
                        parent.broadcastMessage(Protocol.LEFT + " " + parent.username);
                        writer.flush();

                        parent.server.users.remove(this);
                        //socket.close();
                        parent.writer.close();
                        parent.reader.close();
                        parent.stop();
                    }else{
                        //Pong received, reset flag
                        pingPongFlag=false;
                    }
                } catch (Exception e) {
                    Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);
                    parent.server.users.remove(this);
                    parent.pingPongThread.stop();
                    parent.stop();
                }
            }
        }
    }
}


