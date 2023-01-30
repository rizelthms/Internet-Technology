package Client;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;

public class Encryption {

    public static KeyPair generateKeyPair() {
        try {
            // Initialize KeyPairGenerator instance with RSA algorithm
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            // Set key size to 1024 bits
            generator.initialize(1024);
            // Generate and return a KeyPair object
            return generator.generateKeyPair();
        }
        catch(Exception e){
            // If any error occurs during key pair generation, print error message and return null
            System.out.println("Error generating key pair: " + e.getMessage());
            return null;
        }
    }

    public static String encryptAES(String input, SecretKey sessionKey){
        try {
            // Initialize Cipher instance with AES algorithm
            Cipher cipher = Cipher.getInstance("AES");
            // Set cipher to encryption mode and use the provided session key
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
            // Encrypt input and return the encoded string using Base64
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes()));
        }
        catch(Exception e){
            // If any error occurs during encryption, print error message and return null
            System.out.println("Error encrypting: " + e.getMessage());
            return null;
        }
    }

    public static String decryptAES(String input, SecretKey key){
        try {
            // Initialize Cipher instance with AES algorithm
            Cipher cipher = Cipher.getInstance("AES");
            // Set cipher to decryption mode and use the provided key
            cipher.init(Cipher.DECRYPT_MODE, key);
            // Decode input using Base64 and return the decrypted string
            return new String(cipher.doFinal(Base64.getDecoder().decode(input)));
        }
        catch(Exception e){
            // If any error occurs during decryption, print error message and return null
            System.out.println("Error decrypting: " + e.getMessage());
            return null;
        }
    }

    public static String encryptRSA(String input, Key publicKey){
        try {
            // Initialize Cipher instance with RSA algorithm
            Cipher cipher = Cipher.getInstance("RSA");
            // Set cipher to encryption mode and use the provided public key
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            // Encrypt input and return the encoded string using Base64
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes()));
        } catch (Exception e) {
            System.out.println("Error encrypting: " + e.getMessage());
            return null;
        }
    }

    public static String decryptRSA(String input, Key privateKey){
        try {
            // Initialize Cipher instance with RSA algorithm
            Cipher cipher = Cipher.getInstance("RSA");
            // Set cipher to decryption mode and use the provided private key
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            // Decode the Base64 encoded string and decrypt it using the private key
            return new String(cipher.doFinal(Base64.getDecoder().decode(input)));
        }
        catch(Exception e){
            System.out.println("Error decrypting: " + e.getMessage());
            return null;
        }
    }
}



