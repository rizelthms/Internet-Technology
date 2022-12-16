import Shared.Printer;
import Shared.Protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
                            //Already logged in
                            writer.println(Protocol.FAIL04 +  "User cannot login twice");
                        }
                    }
                    case Protocol.BROADCAST -> {
                        if(username!=null) {
                            broadcastMessage(String.join(" ", msg));
                        }else{
                            writer.println(Protocol.FAIL03 + " Please login first");
                        }
                    }
                    case Protocol.QUIT -> {
                        // Client is trying to disconnect
                        writer.println(Protocol.OK + " " + Protocol.GOODBYE);
                        server.users.remove(this);
                        writer.close();
                        reader.close();
                        socket.close();
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
                    default -> {
                        //Unknown command
                        writer.println(Protocol.FAIL00 + " Unknown command");
                    }
                }
            }
        }catch (Exception e){
            Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);
            server.users.remove(this);
            pingPongThread.stop();
            this.stop();
        }
    }
    private void broadcastMessage(String msg){
        for(int i=0; i < server.users.size(); i++){
            //Broadcast to all clients except the sender of the message
            if(msg.startsWith(Protocol.BROADCAST) && server.users.get(i)!=this){
                //Reformat string, insert the username of the sender
                server.users.get(i).writer.println(new StringBuilder(msg).insert(Protocol.BROADCAST.length(), " " + this.username).toString());
            }else if(msg.startsWith(Protocol.JOINED) && server.users.get(i)!=this) {
                server.users.get(i).writer.println(msg);
            }else if(msg.startsWith(Protocol.BROADCAST) && server.users.get(i)==this){
                //Confirmation response sent to the client who sent this message
                server.users.get(i).writer.println(Protocol.OK + " " + msg);
            }
        }
    }

    //Sends a list of current users to the client that requested it
    private void userList(){
        String userList = "";
        int loggedInUsers = 0;
        for(int i=0; i < server.users.size(); i++) {
            if(server.users.get(i).username!=null) {
                userList=userList.concat(server.users.get(i).username + ":");
                loggedInUsers++;
            }
        }
        if(loggedInUsers==1){
            writer.println(Protocol.FAIL06 + " YOU ARE THE ONLY CONNECTED USER.");
        }else if(loggedInUsers>0){
            writer.println(Protocol.OK + " " + Protocol.USERS + " " + userList.substring(0, userList.length() - 1)); //substring removes last unnecessary : from the string
            System.out.println(Protocol.OK + " " + Protocol.USERS + " " + userList.substring(0, userList.length() - 1));
        }
    }

    //Sends a private message to the specified user
    private void privateMsg(String msg, String user){
        boolean userExists = false;
        for(int i=0; i < server.users.size(); i++) {
            if(server.users.get(i).username!=null && server.users.get(i).username.equals(user)) {
                writer.println(Protocol.OK + " " + Protocol.PRIVATE_MSG + " " + user + " " + msg); //Confirm/approve the command
                server.users.get(i).writer.println(Protocol.RECEIVE_PRIVATE_MSG + " " + username + " " + msg); //Send the message to the destined receiver
                userExists=true;
                break;
            }
        }
        if(!userExists){
            writer.println(Protocol.FAIL07 + " USER DOES NOT EXIST");
        }
    }

    //Sets the correct username for the user, i.e. creates a new user
    private void createUser(String[] msgArr){
        //Check for errors

        //User already logged in
        for(int i=0; i < server.users.size(); i++){
            if(server.users.get(i)!=this){
                if(server.users.get(i).username!=null && server.users.get(i).username.equals(msgArr[1])){
                    writer.println(Protocol.FAIL01 + " User already logged in");
                    return;
                    //continue outer; //continue the outer (while) loop
                }
            }
        }

        //Invalid Format, contains special characters, only letters, numbers and underscore allowed
        Pattern special = Pattern.compile ("[!@#$%&*()+=|<>?{};:,<.>/\\[\\]~-]");
        Matcher hasSpecial = special.matcher(msgArr[1]);
        if(msgArr[1].length()>14 || msgArr[1].length()<3 || hasSpecial.find()){
            writer.println(Protocol.FAIL02 + " Username has an invalid format or length");
            return;
            //Continue; //continue the outer (while) loop
        }

        //Set username for the client
        username = msgArr[1];
        writer.println(Protocol.OK + " " + Protocol.IDENTIFY + " " +  username);
        broadcastMessage("JOINED " + username);
    }

    private static class pingPong implements Runnable{
        ServerThread parent;

        pingPong(ServerThread parent){
            this.parent=parent;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    parent.writer.println(Protocol.PING);
                    // Sleep for 3 seconds
                    Thread.sleep(3000);

                    if(!pingPongFlag){
                        parent.writer.println(Protocol.DISCONNECT + " Pong timeout");
                        parent.writer.println(Protocol.FAIL05 + " Pong without ping");
                        parent.server.users.remove(this);
                        socket.close();
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