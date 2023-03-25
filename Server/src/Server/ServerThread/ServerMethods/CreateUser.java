package Server.ServerThread.ServerMethods;

import Server.Server;
import Server.Model.ClientConnection;
import Server.ServerThread.ServerThread;
import Shared.Protocol;

public class CreateUser {
    //Sets the correct username for the user, i.e. creates a new user
    public static void createUser(ClientConnection connection, String[] msgArr) {
        //Server.Model.User already logged in
        for (ServerThread userThread: Server.users) {
            if (userThread != connection.getServerThread()) {
                if (userThread.getUsername() != null && userThread.getUsername().equals(msgArr[1])) {
                    connection.getWriter().println(Protocol.FAIL01);
                    return;
                }
            }
        }

        //Invalid Format, contains special characters, only letters, numbers and underscore allowed
        if (msgArr[1].length() > 14 || msgArr[1].length() < 3 || msgArr[1].matches("[!@#$%&*()+=|<>?{};:,<.>/\\[\\]~-]")) {
            connection.getWriter().println(Protocol.FAIL02);
            return;
        }

        //Set username for the client
        connection.setUsername(msgArr[1]);
        connection.getWriter().println(Protocol.OK + " " + Protocol.IDENTIFY + " " + connection.getUsername());
        BroadcastMessage.broadcastMessage(connection, "JOINED " + connection.getUsername());
    }
}
