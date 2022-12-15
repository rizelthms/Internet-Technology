import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

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

    }


}