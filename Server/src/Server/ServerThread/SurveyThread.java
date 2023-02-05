package Server.ServerThread;

import Server.Model.ClientConnection;
import Server.Server;
import Shared.Protocol;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;

public class SurveyThread extends Thread {
    ClientConnection connection;
    ArrayList<String> users;
    String[] question;
    int[] responses;
    boolean isRunning = true;
    boolean hasSentQuestions = false;
    int userCount = 0;
    Instant timeStart;

    public SurveyThread(ClientConnection connection) {
        this.connection = connection;
        this.users = new ArrayList<>();
        // find all users
        Server.users.forEach(user -> users.add(user.getUsername()));
        userCount = users.size();
    }

    public SurveyThread(ClientConnection connection, ArrayList<String> users) {
        this.connection = connection;
        this.users = users;
        userCount = users.size();
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                // if was question wasnt sent yet
                // AND the question is set
                if (!hasSentQuestions
                        && question.length != 0) {
                    // Send messages to all users
                    for (ServerThread user : Server.users) {
                        if (users.stream().anyMatch(userName -> userName.equals(user.getUsername()))) {
                            String question = String.join("|", this.question);
                            user.getWriter().println(Protocol.SURVEY_REQUEST + " " + connection.getUsername() + " " + question);
                        }
                    }

                    // start the timer
                    timeStart = Instant.now();
                    hasSentQuestions = true;
                }
                // if the question was sent,
                // all requested users have responded
                // OR 5 minutes have passed
                else if (hasSentQuestions
                        && (Arrays.stream(this.responses).sum() == userCount
                        || Duration.between(timeStart, Instant.now()).toMinutes() >= 5)) {
                    // send the responses to the all users and end this thread
                    Server.users.forEach(user -> user.getWriter().println(Protocol.OK + " "
                            + Protocol.SURVEY_ANSWER + " "
                            + question[0] + "|"
                            + String.join("|" + Arrays.toString(this.responses))));
                    this.isRunning = false;
                }

                //sleep for 1 second before checking again
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setQuestion(String question) {
        if (this.question.length == 0)
            this.question = question.split("\\|");
    }

    public void setResponse(int response) {
        this.responses[response]++;
    }

    public void stopThread() {
        isRunning = false;
    }
}
