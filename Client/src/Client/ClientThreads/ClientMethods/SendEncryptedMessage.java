package Client.ClientThreads.ClientMethods;

import Client.Model.ClientConnection;
import Shared.Protocol;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;

import static Client.ClientThreads.ClientMethods.Encryption.encryptAES;

public class SendEncryptedMessage {
    public static void sendEncryptedMessage(ClientConnection connection, String message) {
        var messageSplit = message.split(" ");
        // Call shareSessionKey to generate and share the session key if it is not already stored
        Encryption.shareSessionKey(connection, messageSplit[1]);

        try {
            // Encrypt the message using the session key and send it to the recipient
            String msgToEncrypt = String.join(" ", Arrays.copyOfRange(messageSplit, 2, messageSplit.length));
            // Retrieve the session key from the map of session keys
            String strSessionKey = connection.getSessionKeys().get(messageSplit[1]);
            SecretKey sessionKey = new SecretKeySpec(Base64.getDecoder().decode(strSessionKey), "AES");
            // Encrypt the message using the AES algorithm
            String encryptedMessage = encryptAES(msgToEncrypt, sessionKey);
            // Send the encrypted message to the recipient
            connection.getWriter().println(Protocol.PRIVATE_MSG + " " + messageSplit[1] + " " + encryptedMessage);
        } catch (Exception e) {
            System.out.println("Error sending private message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
