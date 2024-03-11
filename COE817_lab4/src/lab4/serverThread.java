package lab4;

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
import java.util.Base64;


public class serverThread extends Thread{

	private Socket sock;
	private String clientID;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	private PublicKey pubServ;
	private PrivateKey privServ;
	private String nonceServ;
	
	public serverThread(Socket sock, PublicKey pub, PrivateKey priv) {
		this.sock = sock;
		this.pubServ = pub;
		this.privServ = priv;
		try {
			out = new ObjectOutputStream(sock.getOutputStream());
			in = new ObjectInputStream(sock.getInputStream());
		} catch (IOException e) {
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
	
	public void run() {
		try {
			clientID = (String) in.readObject();
			System.out.println(clientID + " connected");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
