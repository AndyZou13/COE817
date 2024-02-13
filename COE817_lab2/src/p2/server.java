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
import java.util.Arrays;
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
		byte[] encryptedIn = null;
		byte[] encryptedOut = null;
		byte[] decryptedIn = null;
		
		byte[] encryptedIn1 = null;
		byte[] encryptedIn2 = null;
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
			byte[] part1 = Arrays.copyOfRange(encryptedOut, 0, encryptedOut.length/2);
			byte[] part2= Arrays.copyOfRange(encryptedOut, encryptedOut.length/2, encryptedOut.length);
			ciph.init(Cipher.ENCRYPT_MODE, pubKeyA);
			part1 = ciph.doFinal(part1);
			part2 = ciph.doFinal(part2);
			out.writeObject(part1);
			out.writeObject(part2);
			out.writeObject(nonce);
			
			encryptedIn1 = (byte[]) in.readObject();
			encryptedIn2 = (byte[]) in.readObject();
			System.out.println("Received 3 encrypted: " + encryptedIn1.toString() + encryptedIn2.toString());
			
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.DECRYPT_MODE, privKey);
			encryptedIn1 = ciph.doFinal(encryptedIn1);
			encryptedIn2 = ciph.doFinal(encryptedIn2);
			encryptedIn = new byte[encryptedIn1.length + encryptedIn2.length];
			System.arraycopy(encryptedIn1, 0, encryptedIn, 0, encryptedIn1.length);
			System.arraycopy(encryptedIn2, 0, encryptedIn, encryptedIn1.length, encryptedIn2.length);
			ciph.init(Cipher.DECRYPT_MODE, pubKeyA);
			encryptedIn = ciph.doFinal(encryptedIn);
			System.out.println("Received 3 decrypted: " + new String(encryptedIn));
			
			} catch (SocketException e) {
				System.out.println("Socket timed out.");
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
	}
}
