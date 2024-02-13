package p3;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;

public class client {

	public static void main(String[] args) {

		String id = "Network Security Lab 2 Part 3";
		int port = 50001;
		String host = "localhost";
		Cipher ciph;
		KeyPairGenerator keyGen;
		KeyPair keyPair;
		PublicKey pubKeyA;
		PrivateKey privKey;
		
		try {
			Signature sign = Signature.getInstance("SHA256withRSA");
			Socket client = new Socket (host, port);
			System.out.println("Connected: " + client.getRemoteSocketAddress()); 
			
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyPair = keyGen.genKeyPair();
			privKey = keyPair.getPrivate();
			pubKeyA = keyPair.getPublic();
			
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			out.writeObject(pubKeyA);
			
			sign.initSign(privKey);
			SecureRandom n = new SecureRandom();
			byte[] nBytes = new byte[5];
			n.nextBytes(nBytes);
			out.writeObject(Base64.getEncoder().encodeToString(nBytes));
			String message = id + "|" + Base64.getEncoder().encodeToString(nBytes);
			sign.update(message.getBytes(StandardCharsets.UTF_8));	
			
			byte[] signedMessage = sign.sign();

			out.writeObject(signedMessage);
			
		} catch (SocketException e) {
			System.out.println("Socket timed out.");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
