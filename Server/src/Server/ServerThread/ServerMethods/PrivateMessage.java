package Server.ServerThread.ServerMethods;

import Server.Server;
import Server.Model.ClientConnection;
import Server.ServerThread.ServerThread;
import Shared.Protocol;

public class PrivateMessage {
    //Sends a private message to the specified user
    public static void privateMsg(ClientConnection connection, String msg, String user) {
        boolean userExists = false;
        for (ServerThread userThread: Server.users) {
            if (userThread.getUsername() != null && userThread.getUsername().equals(user)) {
                connection.getWriter().println(Protocol.OK + " " + Protocol.PRIVATE_MSG + " " + user + " " + msg); //Confirm/approve the command
                userThread.getWriter().println(Protocol.RECEIVE_PRIVATE_MSG + " " + connection.getUsername() + " " + msg); //Send the message to the destined receiver
                userExists = true;
                break;
            }
        }
        if (!userExists) {
            connection.getWriter().println(Protocol.FAIL07);
        }
    }
}
