package Client.Model;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private boolean isLoggedIn = false;
    ArrayList<User> users = new ArrayList<>();
    private String lastSurveyUser = "";

    public ClientConnection(Socket socket, PrintWriter writer, BufferedReader reader) {
        this.socket = socket;
        this.writer = writer;
        this.reader = reader;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public String getLastSurveyUser() {
        return lastSurveyUser;
    }

    public void setLastSurveyUser(String lastSurveyUser) {
        this.lastSurveyUser = lastSurveyUser;
    }
}
