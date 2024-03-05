package p1;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class serverThread extends Thread {
	private ArrayList<serverThread> clients = new ArrayList<serverThread>();
	private Socket sock;
	private String clientID;
	private String serverID = "KDC";
	private String nonceServ;
	private String nonce;
	private PublicKey pubKey;
	private Cipher ciph;
	private byte[] encrypted;
	private byte[] decrypted;
	private byte[] encryptedIn;
	private KeyPairGenerator keyGen;
	private KeyPair keyPair;
	private PublicKey pubServ;
	private PrivateKey privServ;
	private String masterKey = "key";
	public serverThread(Socket socket, ArrayList<serverThread> threads) {
		this.clients = threads;
		this.sock = socket;
	}
	private void generateKeyPair() {
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyPair = keyGen.genKeyPair();
			privServ = keyPair.getPrivate();
			pubServ = keyPair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}	
	}
	
	public void generateNonce () {
		SecureRandom n = new SecureRandom();
		byte[] nBytes = new byte[5];
		n.nextBytes(nBytes);
		nonceServ = Base64.getEncoder().encodeToString(nBytes);
	}
	
	public void encodeRSA (String message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.ENCRYPT_MODE, pubKey);
			encrypted = ciph.doFinal(message.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void decodeRSA (byte[] message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.DECRYPT_MODE, pubKey);
			decrypted = ciph.doFinal(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			generateNonce();
			generateKeyPair();
			
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			clientID = (String) in.readObject();
			masterKey += clientID;
			
			in = new ObjectInputStream(sock.getInputStream());
			pubKey = (PublicKey) in.readObject();
			
			ObjectOutputStream out= new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(pubServ);
			
			String message = nonceServ + "|" + serverID;
			encodeRSA(message);
			out.writeObject(encrypted);
			
			encryptedIn = (byte[]) in.readObject();
			decodeRSA(encryptedIn);
			
			message = new String(encryptedIn);
			String[] arr = message.split("\\|");
			nonce = arr[0];
			if (arr[1] != nonceServ) {
				sock.close();
				return;
			}
			
			encodeRSA(nonceServ);
			out.writeObject(encrypted);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
