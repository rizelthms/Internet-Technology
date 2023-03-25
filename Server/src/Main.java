import Shared.Printer;
import Server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // Print a message to the console indicating that the server has started and is waiting for clients to connect
        Printer.printLineColourUnderline("Server started, awaiting clients", Printer.ConsoleColour.WHITE);
        // Start a new server instance, listening on the specified IP address and port
        new Server("127.0.0.1", 1337);
    }
}
