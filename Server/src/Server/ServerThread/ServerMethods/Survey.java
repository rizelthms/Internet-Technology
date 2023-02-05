package Server.ServerThread.ServerMethods;

import Server.Model.ClientConnection;
import Server.Server;
import Server.ServerThread.SurveyThread;
import Shared.Protocol;

import java.util.ArrayList;
import java.util.Arrays;

public class Survey {
    public static String surveyStart(ClientConnection connection, String[] message) {
        if (connection.getUsername() != null) {
            if (connection.getSurveyThread() == null) {
                ArrayList<String> users = new ArrayList<>(Arrays.asList(message).subList(1, message.length));

                if (users.size() < 3)
                    return Protocol.FAIL08;

                connection.setSurveyThread(new SurveyThread(connection, users));
                return Protocol.OK + " " + Protocol.START_SURVEY;
            } else {
                return Protocol.FAIL09;
            }
        } else {
            return Protocol.FAIL03;
        }
    }

    public static String surveyStart(ClientConnection connection) {
        if (connection.getUsername() != null) {
            if (connection.getSurveyThread() == null) {
                if (Server.users.size() < 3)
                    return Protocol.FAIL08;

                connection.setSurveyThread(new SurveyThread(connection));
                return Protocol.OK + " " + Protocol.START_SURVEY_OPEN;
            } else {
                return Protocol.FAIL09;
            }
        } else {
            return Protocol.FAIL03;
        }
    }

    public static String surveyQuestion(ClientConnection connection, String message) {
        if (connection.getUsername() != null) {
            if (connection.getSurveyThread() != null) {
                // remove first 4 characters and send as question
                connection.getSurveyThread().setQuestion(message.substring(4));

                return Protocol.OK + " " + Protocol.SURVEY_QUESTION;
            } else {
                return Protocol.FAIL10;
            }
        } else {
            return Protocol.FAIL03;
        }
    }

    public static String surveyAnswer(ClientConnection connection, String[] message) {
        if (connection.getUsername() != null) {
            if (connection.getSurveyThread() != null) {
                Server.users.stream()
                        .filter(user -> user.getUsername().equals(message[1]))
                        .findFirst()
                        .orElseThrow()
                        .setSurveyResponse(message[2]);

                return Protocol.OK + " " + Protocol.SURVEY_ANSWER;
            } else {
                return Protocol.FAIL10;
            }
        } else {
            return Protocol.FAIL03;
        }
    }

    public static void surveyResponse(ClientConnection connection, String response) {
        if (connection.getUsername() != null) {
            if (connection.getSurveyThread() != null) {
                connection.getSurveyThread().setResponse(Integer.getInteger(response));
            }
        }
    }
}
