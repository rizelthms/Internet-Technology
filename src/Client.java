import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public Client(String ip, int port) throws IOException {
        setSocket(new Socket(ip, port));
        setWriter(new PrintWriter(getSocket().getOutputStream()));
        setReader(new BufferedReader(new InputStreamReader(getSocket().getInputStream())));
        getReaderThread().start();
        getWriterThread().start();
    }

    private final Thread readerThread = new Thread(() -> {
        try {
            reader();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    });

    private final Thread writerThread = new Thread(() -> {
        try {
            writer();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    });

    private void writer() throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            switch (message) {
                case "HELP" -> help();
                default -> {
                    getWriter().println(message);
                    getWriter().flush();
                }
            }
        }
    }

    private void reader() throws IOException, InterruptedException {
        while (true) {
            String message = getReader().readLine();
            switch (message) {
                case "PING":
                    pong();
                    break;
                case "OK Goodbye":
                    System.out.println(message);
                    getReader().close();
                    getWriter().close();
                    getSocket().close();
                    System.exit(0);
                default:
                    System.out.println(message);
                    break;
            }
        }
    }

    public void pong() {
        getWriter().println("PONG");
        getWriter().flush();
    }

    private void help() {
        System.out.println(
                """
                Welcome to the Java chat server!
                -------
                IDENT <username>
                for logging in.
                QUIT
                logs current user out.
                BCST <message>
                broadcasts message to all users that are logged in.""");
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public Thread getReaderThread() {
        return readerThread;
    }

    public Thread getWriterThread() {
        return writerThread;
    }
}