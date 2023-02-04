package Server.ServerThread.ServerMethods;

import Server.Server;
import Server.Model.ClientConnection;
import Server.ServerThread.ServerThread;
import Shared.Protocol;

public class BroadcastMessage {
    public static void broadcastMessage(ClientConnection connection, String msg) {
        for (ServerThread userThread: Server.users) {
            //Broadcast to all clients except the sender of the message
            if (msg.startsWith(Protocol.BROADCAST) && userThread != connection.getServerThread()) {
                //Reformat string, insert the username of the sender
                userThread.getWriter().println(new StringBuilder(msg).insert(Protocol.BROADCAST.length(), " " + connection.getUsername()).toString());
            } else if (msg.startsWith(Protocol.JOINED) && userThread != connection.getServerThread()) {
                userThread.getWriter().println(msg);
            } else if (msg.startsWith(Protocol.BROADCAST) && userThread == connection.getServerThread()) {
                //Confirmation response sent to the client who sent this message
                userThread.getWriter().println(Protocol.OK + " " + msg);
            }
        }
    }
}
