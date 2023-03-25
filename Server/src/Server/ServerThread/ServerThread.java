package Server.ServerThread;

import Server.Server;
import Server.Model.ClientConnection;
import Server.ServerThread.ServerMethods.*;
import Shared.Printer;
import Shared.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class ServerThread extends Thread {
    //Socket to communicate with the client
    protected static Socket socket;
    private ClientConnection connection;
    private PingPongThread pingPongThread;

    public PrintWriter getWriter() {
        return connection.getWriter();
    }

    public String getUsername() {
        return connection.getUsername();
    }

    public void setSurveyResponse(String response) {
        Survey.surveyResponse(connection, response);
    }

    public ServerThread(Socket clientSocket, Server server) {
        try {
            socket = clientSocket;

            // Create the output and input streams for the Server.ServerThread.ServerThread
            this.connection = new ClientConnection(
                    this,
                    new PrintWriter(socket.getOutputStream(), true),
                    new BufferedReader(new InputStreamReader(socket.getInputStream())),
                    server);

            //Send welcome message and read message from client
            this.connection.getWriter().println(Protocol.INITIALISE + " Welcome to the server!");

            //Start a new thread to handle the ping-pong(heartbeat) mechanism for the client
            this.pingPongThread = new PingPongThread(this);
            this.pingPongThread.start();
        } catch (Exception e) {
            Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);

            this.stopThread();
        }
    }

    public void run() {
        try {
            // Listen for incoming messages in an infinite loop
            while (true) {
                //Read message from client
                String messageFull = connection.getReader().readLine();

                if (messageFull == null)
                    continue;

                var msg = messageFull.split(" ");

                // Determine which command the client is trying to use
                switch (msg[0]) {
                    case Protocol.PONG -> {
                        pingPongThread.setHasPonged(true);
                    }
                    case Protocol.IDENTIFY -> {
                        System.out.println("got here");
                        //If the user is new
                        if (connection.getUsername() == null) {
                            CreateUser.createUser(connection, msg);
                        } else {
                            //Already logged in
                            connection.getWriter().println(Protocol.FAIL04);
                        }
                    }
                    case Protocol.BROADCAST -> {
                        if (connection.getUsername() != null) {
                            BroadcastMessage.broadcastMessage(connection, String.join(" ", msg));
                        } else {
                            connection.getWriter().println(Protocol.FAIL03);
                        }
                    }
                    case Protocol.QUIT -> {
                        // Client is trying to disconnect
                        connection.getWriter().println(Protocol.OK + " " + Protocol.GOODBYE);

                        this.stopThread();
                        return;
                    }
                    case Protocol.USERS -> {
                        //Client has requested a list of connected users
                        UserList.userList(connection);
                    }
                    case Protocol.PRIVATE_MSG -> {
                        if (connection.getUsername() != null) {
                            // Client is trying to send a private message
                            PrivateMessage.privateMsg(connection, String.join(" ", Arrays.copyOfRange(msg, 2, msg.length)), msg[1]);
                        } else {
                            connection.getWriter().println(Protocol.FAIL03);
                        }
                    }
                    case Protocol.SEND_PUBLIC_KEY -> {
                        // Broadcast the public key to all users
                        BroadcastMessage.broadcastMessage(connection, messageFull);
                    }
                    case Protocol.SEND_SESSION_KEY -> {
                        // Share session key with other client
                        shareSessionKey(messageFull);
                    }
                    case Protocol.START_SURVEY -> {
                        connection.getWriter().println(Survey.surveyStart(connection, msg));
                    }
                    case Protocol.START_SURVEY_OPEN -> {
                        connection.getWriter().println(Survey.surveyStart(connection));
                    }
                    case Protocol.SURVEY_QUESTION -> {
                        connection.getWriter().println(Survey.surveyQuestion(connection, messageFull));
                    }
                    case Protocol.SURVEY_ANSWER -> {
                        connection.getWriter().println(Survey.surveyAnswer(connection, msg));
                    }
                    default -> {
                        //Unknown command
                        connection.getWriter().println(Protocol.FAIL00);
                    }
                }
            }
        } catch (Exception e) {
            Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);

            this.stopThread();
        }
    }

    private void shareSessionKey(String msg) {
        String[] msgArr = msg.split(" ");

        for (int i = 0; i < Server.users.size(); i++) {
            if (Server.users.get(i).getUsername() != null && Server.users.get(i).getUsername().equals(msgArr[1])) {
                Server.users.get(i).getWriter().println(Protocol.SEND_SESSION_KEY + " " + connection.getUsername() + " " + msgArr[2]);
                break;
            }
        }
    }

    public void stopThread() {
        try {
            //remove from active users
            Server.users.remove(this);
            //stop ping pong thread
            pingPongThread.stopThread();

            //if running survey, stop survey
            if (connection.getSurveyThread() != null)
                connection.getSurveyThread().stopThread();

            //close all connections
            connection.getWriter().close();
            connection.getReader().close();
            socket.close();
            this.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}