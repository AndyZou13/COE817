package lab4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;

import lab4.serverThread;

public class KDC {
	static ArrayList<serverThread> threads = new ArrayList<serverThread>();
	static ArrayList<String> semKeys = new ArrayList<String>(Collections.nCopies(100, ""));
	private static KeyPairGenerator keyGen;
	private static KeyPair keyPair;
	private static PublicKey pubServ;
	private static PrivateKey privServ;
	private static String sharedKey = "KDCshared";
	private static void generateKeyPair() {
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyPair = keyGen.genKeyPair();
			privServ = keyPair.getPrivate();
			pubServ = keyPair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}	
	}
	public static void main(String[] args) {
		int port = 50000;
		ServerSocket s;
		Socket server;
		generateKeyPair();
		try {
			s = new ServerSocket(port);
			while (true) {
				server = s.accept();
				serverThread thread = new serverThread(server, pubServ, privServ, sharedKey);
				threads.add(thread);
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
