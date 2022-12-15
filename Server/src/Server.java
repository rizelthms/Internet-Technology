// Import classes for working with sockets and a list of connected clients
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
    // Create a list for storing the threads handling connected clients
    public static ArrayList<ServerThread> users = new ArrayList<ServerThread>();

    public Server(String ip, int port) throws IOException{
        try {
            // Convert the IP address to an InetAddress object
            InetAddress addr = InetAddress.getByName(ip);
            // Enter an infinite loop to continuously accept new client connections
            while (true) {
                // Create a new server socket and bind it to the specified IP address and port
                serverSocket = new ServerSocket(port, 0, addr); //backlog 0 will set the default backlog (SOMAXCONN on Windows)
                // Listen for a new client to connect
                socket = serverSocket.accept();
                // Print a message indicating that a new client has connected
                System.out.println("New client connected!");
                // Create a new thread for handling the connected client
                ServerThread thread = new ServerThread(socket, this);
                // Add the thread to the list of connected clients
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
