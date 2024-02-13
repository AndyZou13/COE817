package p2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class client {

	public static void main(String[] args) {

		String id = "Alice";
		int port = 50001;
		String host = "localhost";
		String key = "LAB2KEYAB";
		Cipher ciph;
		KeyPairGenerator keyGen;
		KeyPair keyPair;
		PublicKey pubKeyA;
		PublicKey pubKeyB;
		PrivateKey privKey;
		byte[] encryptedIn = null;
		byte[] encryptedIn1 = null;
		byte[] encryptedIn2 = null;
		try {
			Socket client = new Socket (host, port);
			System.out.println("Connected: " + client.getRemoteSocketAddress()); 
			
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyPair = keyGen.genKeyPair();
			privKey = keyPair.getPrivate();
			pubKeyA = keyPair.getPublic();
			ObjectOutputStream out= new ObjectOutputStream(client.getOutputStream());
			out.writeObject(pubKeyA);
			
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			pubKeyB = (PublicKey) in.readObject();
			System.out.println("Public Key B: " + pubKeyB.getEncoded());
			
			SecureRandom n = new SecureRandom();
			byte[] nBytes = new byte[5];
			n.nextBytes(nBytes);
			String nonce = Base64.getEncoder().encodeToString(nBytes);
			String message = id + "|" + nonce;
			
			out.writeObject(message);
			
			encryptedIn1 = (byte[]) in.readObject();
			encryptedIn2 = (byte[]) in.readObject();
			String nonceB = (String) in.readObject();
			System.out.println("Received 2 encrypted: " + encryptedIn1.toString() + encryptedIn2.toString() + "|" + nonceB);
			
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.DECRYPT_MODE, privKey);
			encryptedIn1 = ciph.doFinal(encryptedIn1);
			encryptedIn2 = ciph.doFinal(encryptedIn2);
			encryptedIn = new byte[encryptedIn1.length + encryptedIn2.length];
			System.arraycopy(encryptedIn1, 0, encryptedIn, 0, encryptedIn1.length);
			System.arraycopy(encryptedIn2, 0, encryptedIn, encryptedIn1.length, encryptedIn2.length);
			ciph.init(Cipher.DECRYPT_MODE, pubKeyB);
			encryptedIn = ciph.doFinal(encryptedIn);
			System.out.println("Received 2 decrypted: " + new String(encryptedIn));
			
		} catch (SocketException e) {
			System.out.println("Socket timed out.");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
