import Client.Client;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        new Client("127.0.0.1", 1337);
    }
}
