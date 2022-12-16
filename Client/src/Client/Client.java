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
            // Create socket and connect to server
            socket = new Socket(ip, port);
            // Initialize writer for sending messages to server
            writer = new PrintWriter(socket.getOutputStream());
            // Initialize reader for reading messages from server
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //init reader
            new Thread(() -> {
                try {
                    reader();
                } catch (Exception e) {
                    Printer.printLineColour("Problems connecting to the server!", Printer.ConsoleColour.RED);
                    Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);
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
                }else if (!isLoggedIn) {
                    isLoggedIn = true;

                    writer.println(Protocol.IDENTIFY + " " + message);
                }else if(message.startsWith(Protocol.USERS)){
                    Printer.printLineColourBold("List of connected users:", Printer.ConsoleColour.YELLOW);
                    writer.println(message);
                }else if(message.startsWith(Protocol.PRIVATE_MSG)){
                    writer.println(message);
                }
                else{
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
                        Printer.printLineColourBold(
                                "Welcome " + message[2] + "!",
                                Printer.ConsoleColour.GREEN);
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
                        // select 1 of 4 colours (the last four are for server messages)
                        users.add(
                                new User(
                                        message[1],
                                        Printer.ConsoleColour.values()[(users.size() % 4)]));

                    Printer.printLineColour(
                            "[" + message[1] + "] " + String.join(" ", Arrays.copyOfRange(message, 2, message.length)),
                            users.stream().filter(user -> user.username().equals(message[1])).findFirst().get().colour());
                }
                case Protocol.RECEIVE_PRIVATE_MSG -> {
                    Printer.printLineColour("PRIVATE MESSAGE " + "[" + message[1] + "] " + String.join(" ", Arrays.copyOfRange(message, 2, message.length)), Printer.ConsoleColour.PURPLE);
                }
                case Protocol.JOINED -> {
                    if(isLoggedIn) {
                        Printer.printLineColourBold(message[1] + " has joined the chat!", Printer.ConsoleColour.GREEN);
                    }
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
}