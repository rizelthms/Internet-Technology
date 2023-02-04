package Server.ServerThread.ServerMethods;

import Server.Server;
import Server.Model.ClientConnection;
import Server.ServerThread.ServerThread;
import Shared.Protocol;

public class UserList {
    //Sends a list of current users to the client that requested it
    public static void userList(ClientConnection connection) {
        String userList = "";
        int loggedInUsers = 0;
        for (ServerThread userThread: Server.users) {
            if (userThread.getUsername() != null) {
                userList = userList.concat(userThread.getUsername() + ":");
                loggedInUsers++;
            }
        }
        if (loggedInUsers == 1) {
            connection.getWriter().println(Protocol.FAIL06);
        } else if (loggedInUsers > 0) {
            String s = Protocol.OK + " " + Protocol.USERS + " " + userList.substring(0, userList.length() - 1);
            connection.getWriter().println(s); //substring removes last unnecessary : from the string
            System.out.println(s);
        }
    }
}
