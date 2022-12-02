import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;

    public Client(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        writer = new PrintWriter(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //init reader
        Thread readerThread = new Thread(() -> {
            try {
                reader();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        readerThread.start();

        //init writer
        Thread writerThread = new Thread(() -> {
            try {
                writer();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        writerThread.start();
    }

    private void writer() throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        boolean isLoggedIn = false;

        while (true) {
            if (!isLoggedIn)
                System.out.printf("username: ");

            String message = scanner.nextLine();

            if ("HELP".equals(message)) {
                help();
            } else if (!isLoggedIn) {
                isLoggedIn = true;

                writer.println("IDENT " + message);
                writer.flush();
            } else {
                writer.println("BCST " + message);
                writer.flush();
            }
        }
    }

    private void reader() throws IOException, InterruptedException {
        while (true) {
            String message = reader.readLine();
            switch (message) {
                case "PING" -> {
                    writer.println("PONG");
                    writer.flush();
                }
                case "OK Goodbye" -> {
                    System.out.println(message);
                    reader.close();
                    writer.close();
                    socket.close();
                    System.exit(0);
                }
                default -> System.out.println(message);
            }
        }
    }

    private void help() {
        Helper.printLineColour(
                """
                        Welcome to the Java chat server!
                        -------
                        IDENT <username>
                        for logging in.
                        QUIT
                        logs current user out.
                        BCST <message>
                        broadcasts message to all users that are logged in.""",
                Helper.ConsoleColour.YELLOW);
    }
}