package p1;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

import javax.crypto.SecretKeyFactory;

public class server {	
	
	public static void main(String[] args) {
		String id = "Bob";
		int port = 50000;
		ServerSocket s;
		String keyAB = "lab2";
		try {
			s = new ServerSocket(port);
			Socket server = s.accept();
			System.out.println("Connected: " + server.getRemoteSocketAddress());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
			String messageIn = in.readLine();
			String nonceA = messageIn.substring(messageIn.length()-7, messageIn.length());
			String clientID = messageIn.substring(0, messageIn.length()-7);
			
			SecureRandom n = new SecureRandom();
			byte[] nBytes = new byte[5];
			n.nextBytes(nBytes);
			String nonceB = id + Base64.getEncoder().encodeToString(nBytes);
			byte[] b = keyAB.getBytes();
			SecretKeyFactory fact = SecretKeyFactory.getInstance("DES");
			
			String messageOut
		} catch (Exception e) {
			System.out.println("Error");
		}
	}

}
