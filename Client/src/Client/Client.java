package Client;

import Shared.Printer;
import Shared.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Client extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean isLoggedIn = false;
    ArrayList<User> users = new ArrayList<>();

    public Client(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //init reader
            new Thread(() -> {
                try {
                    reader();
                } catch (IOException | InterruptedException e) {
                    Printer.printLineColour("Problems connecting to the server!", Printer.ConsoleColour.RED);
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
                Printer.printColourBold("username: ", Printer.ConsoleColour.WHITE);

            String message = scanner.nextLine();

            if (!message.isEmpty()) {
                if (message.startsWith(Protocol.QUIT)) {
                    writer.println(message);
                } else if (!isLoggedIn) {
                    isLoggedIn = true;

                    writer.println(Protocol.IDENTIFY + " " + message);
                } else {
                    writer.println(Protocol.BROADCAST + " " + message);
                }
                writer.flush();
            }
        }
    }

    private void reader() throws IOException, InterruptedException {
        while (true) {
            var message = reader.readLine().split(" ");

            switch (message[0]) {
                case Protocol.OK -> {
                    if (message[1].equals(Protocol.GOODBYE)) {
                        System.out.println("Cya next time");

                        reader.close();
                        writer.close();
                        socket.close();
                        System.exit(0);

                    } else if (message[1].equals(Protocol.IDENTIFY)) {
                        Printer.printLineColourBold(
                                "Welcome " + message[2] + "!",
                                Printer.ConsoleColour.GREEN);
                    }
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
                        // select 1 of 4 colours (the last four are for server messages)
                        users.add(
                                new User(
                                        message[1],
                                        Printer.ConsoleColour.values()[(users.size() % 4)]));

                    Printer.printLineColour(
                            "[" + message[1] + "] " + String.join(" ", Arrays.copyOfRange(message, 2, message.length)),
                            users.stream().filter(user -> user.username().equals(message[1])).findFirst().get().colour());
                }
                // all fails
                default -> {
                    Printer.printLineColour(
                            String.join(" ", Arrays.copyOfRange(message, 1, message.length)),
                            Printer.ConsoleColour.RED);

                    if (message[0].equals(Protocol.FAIL01) || message[0].equals(Protocol.FAIL02)) {
                        isLoggedIn = false;
                        Printer.printColourBold("username: ", Printer.ConsoleColour.WHITE);
                    }
                }
            }
        }
    }
}