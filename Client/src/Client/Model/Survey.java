package Client.Model;

import Shared.Printer;
import Shared.Protocol;

import java.util.ArrayList;
import java.util.Scanner;

public class Survey {
    public static void startSurvey(ClientConnection connection, Scanner scanner) {
        sendUsers(connection, scanner);
        sendQuestion(connection, scanner);
    }

    private static void sendUsers(ClientConnection connection, Scanner scanner) {
        Printer.printLineColour("Please enter which users should recieve the survey\n" +
                        "(x ends the input, no input means all users are eligible)",
                Printer.ConsoleColour.WHITE);

        ArrayList<String> users = new ArrayList<>();
        String user = "";

        while (!user.equalsIgnoreCase("x")) {
            Printer.printColour("User: ", Printer.ConsoleColour.WHITE);
            user = scanner.nextLine();
            if (!user.equalsIgnoreCase("x"))
                users.add(user);
        }

        if (users.size() == 0)
            connection.getWriter().println(Protocol.START_SURVEY_OPEN);
        else
            connection.getWriter().println(Protocol.START_SURVEY + " " + String.join(" " + users));
    }

    private static void sendQuestion(ClientConnection connection, Scanner scanner) {
        Printer.printColourBold("Please enter your question: ", Printer.ConsoleColour.WHITE);
        String question = scanner.nextLine();
        ArrayList<String> answers = new ArrayList<>();
        String answer = "";

        Printer.printLineColour("Please enter your answers\n (x ends the input)", Printer.ConsoleColour.WHITE);

        while (!answer.equalsIgnoreCase("x")) {
            Printer.printColour("Answer: ", Printer.ConsoleColour.WHITE);
            answer = scanner.nextLine();
            if (!answer.equalsIgnoreCase("x"))
                answers.add(answer);
        }

        connection.getWriter().println(Protocol.SURVEY_QUESTION
                + " " + question + "|"
                + String.join("|", answers));
    }
}
