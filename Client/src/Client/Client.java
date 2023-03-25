package Client;

import Client.ClientThreads.ClientReaderThread;
import Client.ClientThreads.ClientWriterThread;
import Client.Model.ClientConnection;
import Shared.Printer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

import static Client.ClientThreads.ClientMethods.Encryption.*;


public class Client extends Thread {
    ClientConnection connection;

    public Client(String ip, int port) {
        try {
            // Create socket and connect to server
            Socket socket = new Socket(ip, port);

            connection = new ClientConnection(socket,
                    // Initialize writer for sending messages to server
                    new PrintWriter(socket.getOutputStream()),
                    // Initialize reader for reading messages from server
                    new BufferedReader(new InputStreamReader(socket.getInputStream())),
                    // Initialize the publicKeys HashMap
                    new HashMap<String, String>(),
                    // Initialize the sessionKeys HashMap
                    new HashMap<String, String>(),
                    //Generate RSA keys
                    generateKeyPair());

            // init reader
            new ClientReaderThread(connection);

            // init writer
            new ClientWriterThread(connection);

        } catch (Exception e) {
            Printer.printLineColour("Problems connecting to the server!", Printer.ConsoleColour.RED);
        }
    }
}
