package lab4;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;


public class serverThread extends Thread{

	private Socket sock;
	private String clientID;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String serverID = "KDC";
	
	private PublicKey pubServ;
	private PrivateKey privServ;
	private PublicKey pub;
	private String sharedKey;
	private String nonce;
	
	private byte[] encrypted;
	private byte[] decrypted;
	private byte[] encryptedIn;
	
	private Cipher ciph;
	
	public serverThread(Socket sock, PublicKey pub, PrivateKey priv, String sharedKey) {
		this.sock = sock;
		this.pubServ = pub;
		this.privServ = priv;
		this.sharedKey = sharedKey;
		try {
			out = new ObjectOutputStream(this.sock.getOutputStream());
			in = new ObjectInputStream(this.sock.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 
	public String getClientID() {
		return clientID;
	}
	
	public void generateNonce () {
		SecureRandom n = new SecureRandom();
		byte[] nBytes = new byte[5];
		n.nextBytes(nBytes);
		nonce = Base64.getEncoder().encodeToString(nBytes);
		System.out.println("Server nonce: " + nonce);
	}
	
	public void encodeRSA (String message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.ENCRYPT_MODE, pub);
			encrypted = ciph.doFinal(message.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void encodeRSA (byte[] message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.ENCRYPT_MODE, pub);
			encrypted = ciph.doFinal(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void encodeRSAserv (String message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.ENCRYPT_MODE, privServ);
			encrypted = ciph.doFinal(message.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void decodeRSA (byte[] message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.DECRYPT_MODE, privServ);
			decrypted = ciph.doFinal(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void clientConnect() {
		try {
			generateNonce();
			
			clientID = (String) in.readObject();
			pub = (PublicKey) in.readObject();
			System.out.println(clientID + " connected");
			out.writeObject(pubServ);
			
			String message = nonce + "|" + serverID;
			encodeRSA(message);
			out.writeObject(encrypted);
			
			encryptedIn = (byte[]) in.readObject();
			decodeRSA(encryptedIn);
			System.out.println("Decrypted: " + new String(decrypted));
			String[] arr = new String(decrypted).split("\\|");
			if (arr[1].equals(nonce) == false) {
				System.out.println("Session key invalid");
				return;
			}
			
			encodeRSA(nonce);
			out.writeObject(encrypted);
			
			message = sharedKey;
			encodeRSAserv(message);
			byte[] part1 = Arrays.copyOfRange(encrypted, 0, encrypted.length/2);
			byte[] part2 = Arrays.copyOfRange(encrypted, encrypted.length/2, encrypted.length);
			encodeRSA(part1);
			out.writeObject(encrypted);
			
			encodeRSA(part2);
			out.writeObject(encrypted);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendClientMessage(byte[] m, PublicKey pub) {
		try {
			out.writeObject(pub);
			out.writeObject(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void run() {
		try {
			clientConnect();
			
			while (true) {
				byte[] message = (byte[]) in.readObject();
				KDC.sendMessage(clientID, message, pub);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
