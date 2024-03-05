package p1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class clientB {
	static String id = "clientB";
	static String host = "localhost";
	static int port = 50000;
	static Socket client;
	static Cipher ciph;
	static byte[] encrypted;
	static byte[] decryptedHolder;
	static byte[] decrypted;
	static byte[] encryptedIn;
	static KeyPairGenerator keyGen;
	static KeyPair keyPair;
	static PublicKey pub;
	static PrivateKey priv;
	static PublicKey pubServ;
	static String nonce;
	static String nonceServ;
	static String masterKey;
	public static void generateNonce () {
		SecureRandom n = new SecureRandom();
		byte[] nBytes = new byte[5];
		n.nextBytes(nBytes);
		nonce = Base64.getEncoder().encodeToString(nBytes);
		System.out.println("Client nonce: " + nonce);
	}
	
	private static void generateKeyPair() {
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyPair = keyGen.genKeyPair();
			priv = keyPair.getPrivate();
			pub = keyPair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}	
	}
	
	public static void encodeRSA (String message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.ENCRYPT_MODE, pubServ);
			encrypted = ciph.doFinal(message.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void decodeRSA (byte[] message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.DECRYPT_MODE, priv);
			decrypted = ciph.doFinal(message);
			System.out.println("Decrypted(privA): " + new String(decrypted));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void decodeRSAserv (byte[] message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.DECRYPT_MODE, pubServ);
			decrypted = ciph.doFinal(message);
			System.out.println("Decrypted(serv): " + new String(decrypted));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		try {
			client = new Socket (host, port);
			System.out.println("Connected: " + client.getRemoteSocketAddress());
			
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			
			generateNonce();
			generateKeyPair();
			out.writeObject(id);
			System.out.println("ID sent: " + id);
			out.writeObject(pub);
			
			pubServ = (PublicKey) in.readObject();
			encryptedIn = (byte[]) in.readObject();
			
			decodeRSA(encryptedIn);
			String message = new String(decrypted);
			String[] arr = message.split("\\|");
			nonceServ = arr[0];
			
			message = nonce + "|" + nonceServ;
			encodeRSA(message);
			
			out.writeObject(encrypted);
			
			encryptedIn = (byte[]) in.readObject();
			decodeRSA(encryptedIn);
			if (new String(decrypted).compareTo(nonceServ) != 0) {
				client.close();
				System.out.println("Session ID does not match, killing process now");
				return;
			}
			
			encryptedIn = (byte[]) in.readObject();
			decodeRSA(encryptedIn);
			byte[] b1 = decrypted;

			encryptedIn = (byte[]) in.readObject();
			decodeRSA(encryptedIn);
			byte[] b2 = decrypted;

			byte[] c = new byte[b1.length + b2.length];
			System.arraycopy(b1, 0, c, 0, b1.length);
			System.arraycopy(b2, 0, c, b1.length, b2.length);

			decodeRSAserv(c);
			masterKey = new String(decrypted);
			
			encrypted = (byte[]) in.readObject();
			SecretKeyFactory fact = SecretKeyFactory.getInstance("DES");
			byte[] b = masterKey.getBytes();
			SecretKey sec = fact.generateSecret(new DESKeySpec(b));
			ciph = Cipher.getInstance("DES/ECB/PKCS5Padding");
			ciph.init(Cipher.DECRYPT_MODE, sec);
			decrypted = ciph.doFinal(encrypted);
			
			System.out.println(new String(decrypted));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
