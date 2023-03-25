package Client.ClientThreads;

import Client.Model.ClientConnection;
import Client.Model.User;
import Shared.Printer;
import Shared.Protocol;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

import static Client.ClientThreads.ClientMethods.Encryption.decryptAES;
import static Client.ClientThreads.ClientMethods.Encryption.decryptRSA;

public class ClientReaderThread {
    private final ClientConnection connection;

    public ClientReaderThread(ClientConnection connection) {
        this.connection = connection;

        //init reader
        new Thread(() -> {
            try {
                reader();
            } catch (Exception e) {
                Printer.printLineColour("Problems connecting to the server!", Printer.ConsoleColour.RED);
                Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);
            }
        }).start();
    }

    private void reader() throws Exception {
        while (true) {
            String messageFull = this.connection.getReader().readLine();
            if (messageFull == null) {
                continue;
            }
            var message = messageFull.split(" ");

            switch (message[0]) {
                case Protocol.OK -> {
                    switch (message[1]) {
                        case Protocol.GOODBYE -> {
                            System.out.println("Cya next time");
                            exit();
                        }
                        case Protocol.IDENTIFY -> Printer.printLineColourBold(
                                "Welcome " + message[2] + "!",
                                Printer.ConsoleColour.GREEN);
                        case Protocol.USERS ->
                            //Display server response to request for user list
                                Printer.printLineColourBold(message[2], Printer.ConsoleColour.YELLOW);
                        case Protocol.START_SURVEY, Protocol.START_SURVEY_OPEN -> Printer.printColourBold("Survey started", Printer.ConsoleColour.GREEN);
                        case Protocol.SURVEY_QUESTION -> Printer.printColourBold("Questions submitted", Printer.ConsoleColour.GREEN);
                    }
                }
                case Protocol.DISCONNECT -> {
                    System.out.println("Disconnected by server!");
                    exit();
                }
                case Protocol.PING -> {
                    this.connection.getWriter().println(Protocol.PONG);
                    this.connection.getWriter().flush();
                }
                case Protocol.INITIALISE -> Printer.printLineColourBold(
                        "You are connected to our chat server!",
                        Printer.ConsoleColour.GREEN);
                case Protocol.BROADCAST -> {
                    if (this.connection.getUsers().stream().noneMatch(user -> user.username().equals(message[1])))
                        // select 1 of 4 colours (the last four are for server messages)
                        this.connection.addUser(
                                new User(
                                        message[1],
                                        Printer.ConsoleColour.values()[(this.connection.getUsers().size() % 4)]));

                    Printer.printLineColour(
                            "[" + message[1] + "] " + String.join(" ", Arrays.copyOfRange(message, 2, message.length)),
                            this.connection.getUsers().stream().filter(user -> user.username().equals(message[1])).findFirst().get().colour());
                }

                case Protocol.RECEIVE_PRIVATE_MSG -> {
                    //Retrieve session key
                    SecretKey sessionKey = new SecretKeySpec(Base64.getDecoder().decode(connection.getSessionKeys().get(message[1])), "AES");

                    //Decrypt message
                    String decryptedMessage = decryptAES(message[2], sessionKey);

                    Printer.printLineColour("PRIVATE MESSAGE " + "[" + message[1] + "] " + decryptedMessage, Printer.ConsoleColour.PURPLE);
                    //Printer.printLineColour("PRIVATE MESSAGE " + "[" + message[1] + "] " + String.join(" ", Arrays.copyOfRange(message, 2, message.length)), Printer.ConsoleColour.PURPLE);
                }
                case Protocol.JOINED -> {
                    if(connection.isLoggedIn()) {
                        Printer.printLineColourBold(message[1] + " has joined the chat!", Printer.ConsoleColour.GREEN);

                        //Share public key
                        connection.getWriter().println(Protocol.SEND_PUBLIC_KEY + " " + Base64.getEncoder().encodeToString(connection.getRSAkeys().getPublic().getEncoded()));
                        connection.getWriter().flush();
                    }
                }
                case Protocol.LEFT -> {
                    if(connection.isLoggedIn()) {
                        Printer.printLineColourBold(message[1] + " has left the chat!", Printer.ConsoleColour.YELLOW);

                        //Delete session key
                        if(connection.getSessionKeys().containsKey(message[1])){
                            connection.getSessionKeys().remove(message[1]);
                        }
                    }
                }
                case Protocol.SEND_PUBLIC_KEY -> {
                    //Store the new users public key
                    connection.getPublicKeys().put(message[1], message[2]);
                    connection.getWriter().println(Protocol.OK + " " + Protocol.SEND_PUBLIC_KEY);
                }
                case Protocol.SEND_SESSION_KEY -> {
                    //Decrypt session key
                    String sessionKey = decryptRSA(message[2], connection.getRSAkeys().getPrivate());
                    connection.getSessionKeys().put(message[1], sessionKey);
                    connection.getWriter().println(Protocol.OK + " " + Protocol.SEND_SESSION_KEY);
                }
                case Protocol.SURVEY_REQUEST ->{
                    Printer.printLineColour(message[1] + " asks you to enter the following survey:", Printer.ConsoleColour.WHITE);
                    connection.setLastSurveyUser(message[2]);

                    String[] question = message[2].split("\\|");
                    Printer.printLineColourBold(String.join("\n", question), Printer.ConsoleColour.WHITE);
                }
                // all fails
                default -> {
                    Printer.printLineColour(
                            String.join(" ", Arrays.copyOfRange(message, 1, message.length)),
                            Printer.ConsoleColour.RED);

                    if (messageFull.equals(Protocol.FAIL01) || messageFull.equals(Protocol.FAIL02) || messageFull.equals(Protocol.FAIL03)) {
                        this.connection.setLoggedIn(false);
                        Printer.printColourBold("Username: ", Printer.ConsoleColour.WHITE);
                    }
                }
            }
        }
    }

    private void exit() throws IOException {
        this.connection.getReader().close();
        this.connection.getWriter().close();
        this.connection.getSocket().close();
        System.exit(0);
    }
}
