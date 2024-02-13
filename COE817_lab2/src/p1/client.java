package p1;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class client {

	public static void main(String[] args) {
		String id = "Alice";
		int port = 50001;
		String host = "localhost";
		String keyAB = "LAB2KEYAB";
		Cipher ciph;
		SecretKey sec;
		byte[] encryptedIn = null;
		byte[] encryptedOut = null;
		byte[] decryptedIn = null;
		try {
			Socket client = new Socket (host, port);
			System.out.println("Connected: " + client.getRemoteSocketAddress()); 
			
			SecureRandom n = new SecureRandom();
			byte[] nBytes = new byte[5];
			n.nextBytes(nBytes);
			String message = id + "|" + Base64.getEncoder().encodeToString(nBytes);
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(message);
			
			
			DataInputStream in = new DataInputStream(client.getInputStream());
			int len = in.readInt();
			if (len > 0) {
				encryptedIn = new byte[len];
			}
			in.read(encryptedIn, 0, len);
			System.out.println("Recieved 2 encrypted: " + encryptedIn.toString());
			
			SecretKeyFactory fact = SecretKeyFactory.getInstance("DES");
			byte[] b = keyAB.getBytes();
			sec = fact.generateSecret(new DESKeySpec(b));
			ciph = Cipher.getInstance("DES/ECB/PKCS5Padding");
			ciph.init(Cipher.DECRYPT_MODE, sec);
			
			decryptedIn = ciph.doFinal(encryptedIn);
			System.out.println("Recieved 2 decrypted: " + new String(decryptedIn));
			String decryp = new String(decryptedIn);
			String[] ar = decryp.split("\\|");
			String messageOut = id + "|" + ar[0];
			
			ciph.init(Cipher.ENCRYPT_MODE, sec);
			encryptedOut = ciph.doFinal(messageOut.getBytes());
			out.writeInt(encryptedOut.length);
			out.write(encryptedOut);
			
		} catch (SocketException e) {
			System.out.println("Socket timed out.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
