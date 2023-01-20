package Client;

import Client.Model.ClientConnection;
import Shared.Printer;
import Shared.Protocol;

import java.io.IOException;
import java.util.Scanner;

public class ClientWriterThread {
    private final ClientConnection connection;

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

            String message = scanner.nextLine();

            if (!message.isEmpty()) {
                if (message.startsWith(Protocol.QUIT)) {
                    this.connection.getWriter().println(message);
                } else if (!this.connection.isLoggedIn()) {
                    this.connection.setLoggedIn(true);

                    this.connection.getWriter().println(Protocol.IDENTIFY + " " + message);
                } else if (message.startsWith(Protocol.USERS)) {
                    Printer.printLineColourBold("List of connected users:", Printer.ConsoleColour.YELLOW);
                    this.connection.getWriter().println(message);
                } else if (message.startsWith(Protocol.PRIVATE_MSG)) {
                    this.connection.getWriter().println(message);
                } else {
                    this.connection.getWriter().println(Protocol.BROADCAST + " " + message);
                }
                this.connection.getWriter().flush();
            }
        }
    }
}
