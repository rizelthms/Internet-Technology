package Client;

import Shared.Printer;
import Shared.Protocol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import static Client.Encryption.*;


public class Client extends Thread {
    private Socket socket;
    private static PrintWriter writer;
    private BufferedReader reader;
    private boolean isLoggedIn = false;
    // List to store connected users
    ArrayList<User> users = new ArrayList<>();
    // KeyPair to store the RSA keys
    KeyPair RSAkeys;
    // HashMap to store public keys of all connected users
    static HashMap<String, String> publicKeys;
    // HashMap to store session keys for each private message communication
    static HashMap<String, String> sessionKeys;

    public Client(String ip, int port) throws IOException {
        try {
            // Create socket and connect to server
            socket = new Socket(ip, port);
            // Initialize writer for sending messages to server
            writer = new PrintWriter(socket.getOutputStream());
            // Initialize reader for reading messages from server
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Initialize the publicKeys HashMap
            publicKeys = new HashMap<String, String>();
            // Initialize the sessionKeys HashMap
            sessionKeys = new HashMap<String, String>();

            //Generate RSA keys
            RSAkeys = generateKeyPair();

            //init reader
            new Thread(() -> {
                try {
                    reader();
                } catch (Exception e) {
                    Printer.printLineColour("Problems connecting to the server!", Printer.ConsoleColour.RED);
                    Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);
                    e.printStackTrace();
                }
            }).start();

            //init writer
            new Thread(() -> {
                try {
                    writer();
                } catch (IOException | InterruptedException e) {
                    Printer.printLineColour("Problems connecting to the server!", Printer.ConsoleColour.RED);
                }
            }).start();
        } catch (Exception e) {
            Printer.printLineColour("Problems connecting to the server!", Printer.ConsoleColour.RED);
        }
    }

    private void writer() throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (!isLoggedIn)
                Printer.printColourBold("Username: ", Printer.ConsoleColour.WHITE);

            String message = scanner.nextLine();

            if (!message.isEmpty()) {
                if (message.startsWith(Protocol.QUIT)) {
                    writer.println(message);
                }else if (!isLoggedIn) {
                    isLoggedIn = true;

                    writer.println(Protocol.IDENTIFY + " " + message);
                }else if(message.startsWith(Protocol.USERS)){
                    Printer.printLineColourBold("List of connected users:", Printer.ConsoleColour.YELLOW);
                    // Send the USERS message to the server
                    writer.println(message);
                }else if(message.startsWith(Protocol.PRIVATE_MSG)){
                    var messageSplit = message.split(" ");
                    // Call shareSessionKey to generate and share the session key if it is not already stored
                    shareSessionKey(messageSplit[1]);

                    try {
                        // Encrypt the message using the session key and send it to the recipient
                        String msgToEncrypt = String.join(" ", Arrays.copyOfRange(messageSplit, 2, messageSplit.length));
                        // Retrieve the session key from the map of session keys
                        String strSessionKey = sessionKeys.get(messageSplit[1]);
                        SecretKey sessionKey = new SecretKeySpec(Base64.getDecoder().decode(strSessionKey), "AES");
                        // Encrypt the message using the AES algorithm
                        String encryptedMessage = encryptAES(msgToEncrypt, sessionKey);
                        // Send the encrypted message to the recipient
                        writer.println(Protocol.PRIVATE_MSG + " " + messageSplit[1] + " " + encryptedMessage);
                    }
                    catch(Exception e){
                        System.out.println("Error sending private message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                else{
                    // If the message is not a special command, send it as a broadcast message
                    writer.println(Protocol.BROADCAST + " " + message);
                }
                writer.flush();
            }
        }
    }

    private void reader() throws Exception{
        while (true) {
            String messageFull = reader.readLine();
            if(messageFull==null){continue;}
            var message = messageFull.split(" ");

            switch (message[0]) {
                case Protocol.OK -> {
                    if (message[1].equals(Protocol.GOODBYE)) {
                        System.out.println("Cya next time");

                        reader.close();
                        writer.close();
                        socket.close();
                        System.exit(0);

                    } else if (message[1].equals(Protocol.IDENTIFY)) {
                        Printer.printLineColourBold("Welcome " + message[2] + "!", Printer.ConsoleColour.GREEN);

                        //Share public key
                        writer.println(Protocol.SEND_PUBLIC_KEY + " " + Base64.getEncoder().encodeToString(RSAkeys.getPublic().getEncoded()));
                        writer.flush();

                    } else if(message[1].equals(Protocol.USERS)){
                        //Display server response to request for user list
                        Printer.printLineColourBold(message[2], Printer.ConsoleColour.YELLOW);
                    }
                }
                case Protocol.DISCONNECT -> {
                    System.out.println("Disconnected by server!");

                    reader.close();
                    writer.close();
                    socket.close();
                    System.exit(0);
                }
                case Protocol.PING -> {
                    writer.println(Protocol.PONG);
                    writer.flush();
                }
                case Protocol.INITIALISE -> Printer.printLineColourBold(
                        "You are connected to our chat server!",
                        Printer.ConsoleColour.GREEN);
                case Protocol.BROADCAST -> {
                    if (users.stream().noneMatch(user -> user.username().equals(message[1])))
                        // Select 1 of 4 colours (the last four are for server messages)
                        users.add(
                                new User(
                                        message[1],
                                        Printer.ConsoleColour.values()[(users.size() % 4)]));

                    Printer.printLineColour(
                            "[" + message[1] + "] " + String.join(" ", Arrays.copyOfRange(message, 2, message.length)),
                            users.stream().filter(user -> user.username().equals(message[1])).findFirst().get().colour());
                }
                case Protocol.RECEIVE_PRIVATE_MSG -> {
                    //Retrieve session key
                    SecretKey sessionKey = new SecretKeySpec(Base64.getDecoder().decode(sessionKeys.get(message[1])), "AES");

                    //Decrypt message
                    String decryptedMessage = decryptAES(message[2], sessionKey);

                    Printer.printLineColour("PRIVATE MESSAGE " + "[" + message[1] + "] " + decryptedMessage, Printer.ConsoleColour.PURPLE);
                    //Printer.printLineColour("PRIVATE MESSAGE " + "[" + message[1] + "] " + String.join(" ", Arrays.copyOfRange(message, 2, message.length)), Printer.ConsoleColour.PURPLE);
                }
                case Protocol.JOINED -> {
                    if(isLoggedIn) {
                        Printer.printLineColourBold(message[1] + " has joined the chat!", Printer.ConsoleColour.GREEN);

                        //Share public key
                        writer.println(Protocol.SEND_PUBLIC_KEY + " " + Base64.getEncoder().encodeToString(RSAkeys.getPublic().getEncoded()));
                        writer.flush();
                    }
                }
                case Protocol.LEFT -> {
                    if(isLoggedIn) {
                        Printer.printLineColourBold(message[1] + " has left the chat!", Printer.ConsoleColour.YELLOW);

                        //Delete session key
                        if(sessionKeys.containsKey(message[1])){
                            sessionKeys.remove(message[1]);
                        }
                    }
                }
                case Protocol.SEND_PUBLIC_KEY -> {
                    //Store the new users public key
                    publicKeys.put(message[1], message[2]);
                    writer.println(Protocol.OK + " " + Protocol.SEND_PUBLIC_KEY);
                }
                case Protocol.SEND_SESSION_KEY -> {
                    //Decrypt session key
                    String sessionKey = decryptRSA(message[2], RSAkeys.getPrivate());
                    sessionKeys.put(message[1], sessionKey);
                    writer.println(Protocol.OK + " " + Protocol.SEND_SESSION_KEY);
                }
                // all fails
                default -> {
                    Printer.printLineColour(
                            String.join(" ", Arrays.copyOfRange(message, 1, message.length)),
                            Printer.ConsoleColour.RED);

                    if (message[0].equals(Protocol.FAIL01) || message[0].equals(Protocol.FAIL02) || message[0].equals(Protocol.FAIL03)) {
                        isLoggedIn = false;
                        Printer.printColourBold("username: ", Printer.ConsoleColour.WHITE);
                    }
                }
            }
        }
    }

    private static void shareSessionKey(String username){
        try {
            // Check if the session key already exists for the username
            if(!sessionKeys.containsKey(username)){
                // Generate a new AES session key
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(128);
                SecretKey sessionKey = keyGenerator.generateKey();
                // Store the encoded session key in the sessionKeys map
                sessionKeys.put(username, Base64.getEncoder().encodeToString(sessionKey.getEncoded()));

                //Encrypt with RSA
                //Change string public key back to public key object
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey pubKey = (PublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeys.get(username))));
                //Encrypt the session key with the public key
                String encryptedSessionKey = Encryption.encryptRSA(Base64.getEncoder().encodeToString(sessionKey.getEncoded()), pubKey);
                //Send the encrypted session key to the recipient
                writer.println(Protocol.SEND_SESSION_KEY + " " + username + " " + encryptedSessionKey);
            }
        }
        catch(Exception e){
            System.out.println("Error generating session key: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
