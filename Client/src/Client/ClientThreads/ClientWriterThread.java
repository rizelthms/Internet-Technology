package Client.ClientThreads;

import Client.ClientThreads.ClientMethods.Encryption;
import Client.ClientThreads.ClientMethods.SendEncryptedMessage;
import Client.Model.ClientConnection;
import Client.Model.Survey;
import Shared.Printer;
import Shared.Protocol;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Scanner;

import static Client.ClientThreads.ClientMethods.Encryption.encryptAES;

public class ClientWriterThread {
    private final ClientConnection connection;
    private final String helpMessage = "Please enter your choice of the following:\n" +
            "       1. Send a broadcast\n" +
            "       2. Show all online users\n" +
            "       3. Send a private message\n" +
            "       4. Send an encrypted private message\n" +
            "       5. Start a survey\n" +
            "       6. Send a file\n" +
            "       7. Answer the last survey\n" +
            "       9. Exit program\n" +
            "       HELP for help";
    Scanner scanner = new Scanner(System.in);

    public ClientWriterThread(ClientConnection connection) {
        this.connection = connection;

        //init writer
        new Thread(() -> {
            try {
                writer();
            } catch (IOException | InterruptedException e) {
                Printer.printLineColour("Problems connecting to the server!", Printer.ConsoleColour.RED);
            }
        }).start();
    }

    private void writer() throws IOException, InterruptedException {
        while (true) {
            if (!this.connection.isLoggedIn()) {
                this.connection.getWriter().println(Protocol.IDENTIFY + " " + getMessage("username: "));
                this.connection.setLoggedIn(true);
                Printer.printLineColourBold(helpMessage, Printer.ConsoleColour.WHITE);
            }

            String selection = getMessage("");

            if (selection.startsWith("9")) {
                this.connection.getWriter().println(Protocol.QUIT);
            } else if (selection.toLowerCase().startsWith("help")) {
                Printer.printLineColourBold(helpMessage, Printer.ConsoleColour.WHITE);
            } else if (selection.startsWith("1")) {
                this.connection.getWriter().println(Protocol.BROADCAST + " " + getMessage("Message to broadcast:"));
            } else if (selection.startsWith("2")) {
                Printer.printLineColourBold("List of connected users:", Printer.ConsoleColour.YELLOW);
                this.connection.getWriter().println(Protocol.USERS);
            } else if (selection.startsWith("3")) {
                this.connection.getWriter().println(Protocol.PRIVATE_MSG + " " + getMessage("Message to send:"));
            } else if (selection.startsWith("4")) {
                SendEncryptedMessage.sendEncryptedMessage(connection, getMessage("Message to send:"));
            } else if (selection.startsWith("5")) {
                Survey.startSurvey(connection, scanner);
            } else if (selection.startsWith("6")) {
                Printer.printLineColour("NOT IMPLEMENTED (Niels' fault)", Printer.ConsoleColour.RED);
            } else if (selection.startsWith("7")) {
                connection.getWriter().println(Protocol.SURVEY_ANSWER + " " + connection.getLastSurveyUser() + " " + getMessage("Survey answer:"));
            } else {
                Printer.printLineColour("Command not recognized!", Printer.ConsoleColour.RED);
            }
        }
    }

    private String getMessage(String messageToPrint) {
        Printer.printLineColourBold(messageToPrint, Printer.ConsoleColour.WHITE);
        String line = "";
        do
            line = scanner.nextLine();
        while (line.isEmpty());

        this.connection.getWriter().flush();
        return line;
    }
}
