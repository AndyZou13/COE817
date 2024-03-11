package lab4;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class clientA {
	
	static Socket client;
	static String host = "localhost";
	static int port = 50000;
	static String nonce;
	static String nonceServ;
	static String clientID = "clientC";
	
	static KeyPairGenerator keyGen;
	static KeyPair keyPair;
	static PrivateKey priv;
	static PublicKey pub;
	static PublicKey pubServ;
	static String sharedKey;
	static SecretKey sec;
	
	static byte[] encrypted;
	static byte[] decrypted;
	static byte[] encryptedIn;
	static byte[] signed;
	
	static Cipher ciph;
	static clientListener listen;

	static ObjectOutputStream out;
	static ObjectInputStream in;
	
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void decodeRSAserv (byte[] message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.DECRYPT_MODE, pubServ);
			decrypted = ciph.doFinal(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void encodeShared (String message) {
		try {
			ciph = Cipher.getInstance("DES/ECB/PKCS5Padding");
			ciph.init(Cipher.ENCRYPT_MODE, sec);
			encrypted = ciph.doFinal(message.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void signMessage(String message) {
		try {
			Signature sign = Signature.getInstance("SHA256withRSA");
			sign.initSign(priv);
			sign.update(message.getBytes(StandardCharsets.UTF_8));
			signed = sign.sign();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void connectKDC () {
		try {
			client = new Socket (host, port);
			System.out.println("Connected: " + client.getRemoteSocketAddress());
		
			out = new ObjectOutputStream(client.getOutputStream());
			in = new ObjectInputStream(client.getInputStream());
			
			generateNonce();
			generateKeyPair();
			
			out.writeObject(clientID);
			out.writeObject(pub);
			pubServ = (PublicKey) in.readObject();
			
			encrypted = (byte[])in.readObject();
			decodeRSA(encrypted);
			System.out.println("Decrypted Recieved: " + new String(decrypted));
		
			String[] arr = new String(decrypted).split("\\|");
			nonceServ = arr[0];
			String message = nonce + "|" + arr[0];
			encodeRSA(message);	
			out.writeObject(encrypted);
			
			encryptedIn = (byte[]) in.readObject();
			decodeRSA(encryptedIn);
			if (new String(decrypted).equals(nonceServ) == false) {
				System.out.println("Session Key Invalid");
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
			sharedKey = new String(decrypted);
			System.out.println("Decrypted Recieved: " + sharedKey);
			
			SecretKeyFactory fact = SecretKeyFactory.getInstance("DES");
			byte[] b = sharedKey.getBytes();
			sec = fact.generateSecret(new DESKeySpec(b));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		connectKDC();
		try {
			listen = new clientListener(in, sharedKey);
			listen.start();
			Scanner s = new Scanner(System.in);
			while(true) {
				System.out.println("What would you like to send to other users?");
				String message = s.nextLine();
				message = clientID + "|" + message;
				
				encodeShared(message);
				signMessage(message);
				byte[] m = new byte[encrypted.length + signed.length];
				System.arraycopy(encrypted, 0, m, 0, encrypted.length);
				System.arraycopy(signed, 0, m, encrypted.length, signed.length);
				out.writeObject(m);
			}
		} catch (Exception e) {
			
		}
	}

}
