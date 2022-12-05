package Client;

import Shared.Helper;

import java.io.*;
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
                    Helper.printLineColour("Problems connecting to the server!", Helper.ConsoleColour.RED);
                }
            }).start();

            //init writer
            new Thread(() -> {
                try {
                    writer();
                } catch (IOException | InterruptedException e) {
                    Helper.printLineColour("Problems connecting to the server!", Helper.ConsoleColour.RED);
                }
            }).start();
        } catch (Exception e) {
            Helper.printLineColour("Problems connecting to the server!", Helper.ConsoleColour.RED);
        }
    }

    private void writer() throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (!isLoggedIn)
                Helper.printColourBold("username: ", Helper.ConsoleColour.WHITE);

            String message = scanner.nextLine();

            if (message.startsWith("QUIT")) {
                writer.println(message);
            } else if (!isLoggedIn) {
                isLoggedIn = true;

                writer.println("IDENT " + message);
            } else {
                writer.println("BCST " + message);
            }
            writer.flush();
        }
    }

    private void reader() throws IOException, InterruptedException {
        while (true) {
            var message = reader.readLine().split(" ");

            switch (message[0]) {
                case "OK" -> {
                    if (message[1].equals("Goodbye")) {
                        System.out.println("Cya next time");

                        reader.close();
                        writer.close();
                        socket.close();
                        System.exit(0);

                    } else if (message[1].equals("IDENT")) {
                        Helper.printLineColourBold(
                                "Welcome " + message[2] + "!",
                                Helper.ConsoleColour.GREEN);
                    }
                }
                case "PING" -> {
                    writer.println("PONG");
                    writer.flush();
                }
                case "INIT" -> Helper.printLineColourBold(
                        "You are connected to our chat server!",
                        Helper.ConsoleColour.GREEN);
                case "BCST" -> {
                    if (users.stream().noneMatch(user -> user.username().equals(message[1])))
                        // select 1 of 4 colours (the last four are for server messages)
                        users.add(
                                new User(
                                        message[1],
                                        Helper.ConsoleColour.values()[(users.size() % 8 % 4)]));

                    Helper.printLineColour(
                            "[" + message[1] + "] " + String.join(" ", Arrays.copyOfRange(message, 2, message.length)),
                            users.stream().filter(user -> user.username().equals(message[1])).findFirst().get().colour());
                }
                // all fails
                default -> {
                    Helper.printLineColour(
                            String.join(" ", Arrays.copyOfRange(message, 1, message.length)),
                            Helper.ConsoleColour.RED);

                    if (message[0].equals("FAIL01") || message[0].equals("FAIL02")) {
                        isLoggedIn = false;
                        Helper.printColourBold("username: ", Helper.ConsoleColour.WHITE);
                    }
                }
            }
        }
    }
}