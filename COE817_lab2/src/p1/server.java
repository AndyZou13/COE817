package p1;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;

public class server {	
	
	public static void main(String[] args) {
		String id = "Bob";
		int port = 50001;
		ServerSocket s;
		String keyAB = "LAB2KEYAB";
		Cipher ciph;
		SecretKey sec;
		byte[] encryptedIn = null;
		byte[] encryptedOut = null;
		byte[] decryptedIn = null;
		try {
			s = new ServerSocket(port);
			Socket server = s.accept();
			System.out.println("Connected: " + server.getRemoteSocketAddress());
			
			DataInputStream in = new DataInputStream(server.getInputStream());
			String message = in.readUTF();
			String[] ar = message.split("\\|");
			System.out.println("Recieved 1: " + message);
			
			SecureRandom n = new SecureRandom();
			byte[] nBytes = new byte[5];
			n.nextBytes(nBytes);
			String nonceB = Base64.getEncoder().encodeToString(nBytes);
			
			SecretKeyFactory fact = SecretKeyFactory.getInstance("DES");
			byte[] b = keyAB.getBytes();
			sec = fact.generateSecret(new DESKeySpec(b));
			ciph = Cipher.getInstance("DES/ECB/PKCS5Padding");
			ciph.init(Cipher.ENCRYPT_MODE, sec);
			
			String messageOut = nonceB + "|" + id + "|" + ar[1];
			encryptedOut = ciph.doFinal(messageOut.getBytes());
			DataOutputStream out = new DataOutputStream(server.getOutputStream());
			out.writeInt(encryptedOut.length);
			out.write(encryptedOut);
			
			int len = in.readInt();
			if (len > 0) {
				encryptedIn = new byte[len];
			}
			in.read(encryptedIn, 0, len);
			System.out.println("Recieved 3 encrypted: " + encryptedIn.toString());
			
			ciph.init(Cipher.DECRYPT_MODE, sec);
			decryptedIn = ciph.doFinal(encryptedIn);
			System.out.println("Recieved 3 decrypted: " + new String(decryptedIn));
			} catch (SocketException e) {
				System.out.println("Socket timed out.");
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}
