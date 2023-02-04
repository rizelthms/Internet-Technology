package Server;
import Server.ServerThread.ServerThread;
import Shared.Printer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {
    // Create a server socket for accepting client connections
    private ServerSocket serverSocket;
    // Create a socket for the current client
    private Socket socket;
    public static ArrayList<ServerThread> users = new ArrayList<ServerThread>();

    public Server(String ip, int port) throws IOException{
        try {
            // Convert the IP address to an InetAddress object
            InetAddress addr = InetAddress.getByName(ip);
            while (true) {
                // Create a new server socket and bind it to the specified IP address and port
                serverSocket = new ServerSocket(port, 0, addr);

                // Listen for a new client to connect
                socket = serverSocket.accept();
                System.out.println("New client connected!");

                // Create a new thread for handling the connected client
                ServerThread thread = new ServerThread(socket, this);
                users.add(thread);

                // Start the thread
                thread.start();
                Thread.sleep(100);
                serverSocket.close();
            }
        } catch (Exception e){
            Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);
        }
    }
}
