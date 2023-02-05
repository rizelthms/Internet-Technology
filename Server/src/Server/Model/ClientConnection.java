package Server.Model;

import Server.Server;
import Server.ServerThread.ServerThread;
import Server.ServerThread.SurveyThread;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ClientConnection {
    private final ServerThread serverThread;
    //Output stream to write messages to the client
    private final PrintWriter writer;
    //Input stream to read message from the client
    private final BufferedReader reader;
    private final Server server;
    private String username = null;
    private SurveyThread surveyThread = null;

    public ClientConnection(ServerThread serverThread, PrintWriter writer, BufferedReader reader, Server server) {
        this.serverThread = serverThread;
        this.writer = writer;
        this.reader = reader;
        this.server = server;
    }

    public ServerThread getServerThread() {
        return serverThread;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public Server getServer() {
        return server;
    }

    public String getUsername() {
        return username;
    }

    public SurveyThread getSurveyThread() {
        return surveyThread;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSurveyThread(SurveyThread surveyThread) {
        this.surveyThread = surveyThread;
    }
}
