package p3;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;

public class server {

	public static void main(String[] args) {
		
		String id = "Network Security Lab 2 Part 3";
		int port = 50001;
		ServerSocket s;
		PublicKey pubKeyA;
		
		byte[] encryptedIn = null;
		String nonce = null;
		try {
			s = new ServerSocket(port);
			Socket server = s.accept();
			System.out.println("Connected: " + server.getRemoteSocketAddress());
			
			ObjectInputStream in = new ObjectInputStream(server.getInputStream());
			
			pubKeyA = (PublicKey)in.readObject();
			System.out.println("Public Key A: " + pubKeyA.getEncoded());
			nonce = (String) in.readObject();
			String verifiedMessage = id + "|" + nonce;
			encryptedIn = (byte[])in.readObject();
			System.out.println("Signed message: " + encryptedIn);
			
			Signature unsign = Signature.getInstance("SHA256withRSA");
			unsign.initVerify(pubKeyA);
			unsign.update(verifiedMessage.getBytes(StandardCharsets.UTF_8));
			
			System.out.println("Identity Verification: " + unsign.verify(encryptedIn));
			} catch (SocketException e) {
				System.out.println("Socket timed out.");
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}
