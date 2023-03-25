package Client.Model;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientConnection {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private boolean isLoggedIn = false;
    ArrayList<User> users = new ArrayList<>();
    private String lastSurveyUser = "";
    // KeyPair to store the RSA keys
    private KeyPair RSAkeys;
    // HashMap to store public keys of all connected users
    private HashMap<String, String> publicKeys;
    // HashMap to store session keys for each private message communication
    private HashMap<String, String> sessionKeys;

    public ClientConnection(Socket socket,
                            PrintWriter writer,
                            BufferedReader reader,
                            HashMap<String, String> publicKeys,
                            HashMap<String, String> sessionKeys,
                            KeyPair RSAkeys) {
        this.socket = socket;
        this.writer = writer;
        this.reader = reader;
        this.publicKeys = publicKeys;
        this.sessionKeys = sessionKeys;
        this.RSAkeys = RSAkeys;
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

    public KeyPair getRSAkeys() {
        return RSAkeys;
    }

    public HashMap<String, String> getPublicKeys() {
        return publicKeys;
    }

    public HashMap<String, String> getSessionKeys() {
        return sessionKeys;
    }
}
