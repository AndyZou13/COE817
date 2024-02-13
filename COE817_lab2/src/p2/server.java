package p2;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class server {

public static void main(String[] args) {
		
		String id = "Bob";
		int port = 50001;
		String key = "LAB2KEYAB";
		ServerSocket s;
		Cipher ciph;
		KeyPairGenerator keyGen;
		KeyPair keyPair;
		PublicKey pubKeyA;
		PublicKey pubKeyB;
		PrivateKey privKey;
		SecretKey sec;
		byte[] envryptedIn = null;
		byte[] encryptedOut = null;
		byte[] decryptedIn = null;
		try {
			s = new ServerSocket(port);
			Socket server = s.accept();
			System.out.println("Connected: " + server.getRemoteSocketAddress());
			
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyPair = keyGen.genKeyPair();
			privKey = keyPair.getPrivate();
			pubKeyB = keyPair.getPublic();
			ObjectOutputStream out= new ObjectOutputStream(server.getOutputStream());
			out.writeObject(pubKeyB);
			
			ObjectInputStream in = new ObjectInputStream(server.getInputStream());
			pubKeyA = (PublicKey) in.readObject();
			System.out.println("Public Key A: " + pubKeyA.getEncoded());
			
			SecureRandom n = new SecureRandom();
			byte[] nBytes = new byte[5];
			n.nextBytes(nBytes);
			String nonce = Base64.getEncoder().encodeToString(nBytes);
			
			String message = (String) in.readObject();
			System.out.println("Received 1: " + message);
			String[] ar = message.split("\\|");
			String nonceA = ar[1];
			
			
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.ENCRYPT_MODE, privKey);
			encryptedOut = ciph.doFinal(nonceA.getBytes());
			System.out.println(encryptedOut.length);
			ciph.init(Cipher.ENCRYPT_MODE, pubKeyA);
			encryptedOut = ciph.doFinal(encryptedOut);
			
			out.writeObject(encryptedOut);
			out.writeObject(nonce);
			
			} catch (SocketException e) {
				System.out.println("Socket timed out.");
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
	}
}
