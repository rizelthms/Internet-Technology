package Client;

import Client.Model.ClientConnection;
import Client.Model.Survey;
import Shared.Printer;
import Shared.Protocol;

import java.io.IOException;
import java.util.Scanner;

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
            "       9. Exit program";

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
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (!this.connection.isLoggedIn())
                Printer.printColourBold("username: ", Printer.ConsoleColour.WHITE);
            else
                Printer.printLineColour(helpMessage, Printer.ConsoleColour.WHITE);

            String selection = scanner.nextLine();
            String message = scanner.nextLine();

            if (!selection.isEmpty()) {
                if (!this.connection.isLoggedIn()) {
                    this.connection.getWriter().println(Protocol.IDENTIFY + " " + message);
                    this.connection.setLoggedIn(true);
                } else if (selection.startsWith("5")) {
                    Survey.startSurvey(connection, scanner);
                }

                if (!selection.matches("[1234679]")) {
                    if (selection.startsWith("9")) {
                        this.connection.getWriter().println(Protocol.QUIT);
                    } else if (selection.startsWith("1")) {
                        this.connection.getWriter().println(Protocol.BROADCAST + " " + message);
                    } else if (selection.startsWith("2")) {
                        Printer.printLineColourBold("List of connected users:", Printer.ConsoleColour.YELLOW);
                        this.connection.getWriter().println(Protocol.USERS + " " + message);
                    } else if (selection.startsWith("3")) {
                        this.connection.getWriter().println(Protocol.PRIVATE_MSG + " " + message);
                    } else if (selection.startsWith("4")) {

                    } else if (selection.startsWith("6")) {
                        Printer.printLineColour("NOT IMPLEMENTED (Niels' fault)", Printer.ConsoleColour.RED);
                    } else if (selection.startsWith("7")) {
                        connection.getWriter().println(Protocol.SURVEY_ANSWER + " " + connection.getLastSurveyUser() + " " + message);
                    }
                }
                this.connection.getWriter().flush();
            } else {
                Printer.printLineColour("Command not recognized!", Printer.ConsoleColour.RED);
            }
        }
    }
}
