package p1;

import java.io.IOException;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class serverThread extends Thread {
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Socket sock;
	private String clientID;
	private String serverID = "KDC";
	private String nonceServ;
	private String nonce;
	private PublicKey pubKey;
	private Cipher ciph;
	private byte[] encrypted;
	private byte[] encryptedHolder;
	private byte[] decrypted;
	private byte[] encryptedIn;
	private KeyPairGenerator keyGen;
	private KeyPair keyPair;
	private PublicKey pubServ;
	private PrivateKey privServ;
	private String masterKey = "key";
	private SecretKey sec;
	private String keyRequest;
	public serverThread(Socket socket) {
		this.sock = socket;
		try {
			out = new ObjectOutputStream(sock.getOutputStream());
			in = new ObjectInputStream(sock.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getClientID() {
		return clientID;
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
		System.out.println("Server nonce: " + nonceServ);
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
	public void encodeRSA (byte[] message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.ENCRYPT_MODE, pubKey);
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
	public void encodeRSAserv (byte[] message) {
		try {
			ciph = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ciph.init(Cipher.ENCRYPT_MODE, privServ);
			encrypted = ciph.doFinal(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void encodeMasterKey (String message) {
		try {
			ciph = Cipher.getInstance("DES/ECB/PKCS5Padding");
			ciph.init(Cipher.ENCRYPT_MODE, sec);
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
			System.out.println("Decrypted: " + new String(decrypted));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendSemKeys(int pos, String id) throws IOException {
		String key = KDC.semKeys.get(pos);
		key = key + "|" + id;
		System.out.println("Key before send: " + key);
		encodeMasterKey(key);
		out.writeObject(encrypted);
	}
	
	@Override
	public void run() {
		try {
			generateNonce();
			generateKeyPair();
			clientID = (String) in.readObject();
			System.out.println(clientID + " connected");
			masterKey += clientID;
			System.out.println("Client: " + clientID);
			pubKey = (PublicKey) in.readObject();
			
			out.writeObject(pubServ);
			
			String message = nonceServ + "|" + serverID;
			encodeRSA(message);
			out.writeObject(encrypted);
			
			encryptedIn = (byte[]) in.readObject();
			decodeRSA(encryptedIn);
			
			message = new String(decrypted);
			String[] arr = message.split("\\|");
			nonce = arr[0];
			if (arr[1].compareTo(nonceServ) != 0) {
				sock.close();
				System.out.println("Session ID does not match, killing process now");
				return;
			}
			
			encodeRSA(nonceServ);
			out.writeObject(encrypted);
			
			SecretKeyFactory fact = SecretKeyFactory.getInstance("DES");
			byte[] b = masterKey.getBytes();
			sec = fact.generateSecret(new DESKeySpec(b));
			
			encodeRSAserv(masterKey);
			byte[] part1 = Arrays.copyOfRange(encrypted, 0, encrypted.length/2);
			byte[] part2 = Arrays.copyOfRange(encrypted, encrypted.length/2, encrypted.length);
			encodeRSA(part1);
			out.writeObject(encrypted);
			
			encodeRSA(part2);
			out.writeObject(encrypted);

			if (clientID.compareTo("clientA") == 0) {
				keyRequest = (String) in.readObject();
				System.out.println(keyRequest);
				String[] ar = keyRequest.split("\\|");
				for (int i = 0; i < KDC.threads.size(); i ++) {
					if (ar[1].compareTo(KDC.threads.get(i).getClientID()) == 0) {
						KDC.semKeys.set(i, "key" + keyRequest);
						sendSemKeys(i, KDC.threads.get(i).getClientID());
						KDC.threads.get(i).sendSemKeys(i, clientID);
					}
				}
			}
			System.out.println("Finished");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
