package p1;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

public class client {

	public static void main(String[] args) {
		String id = "Alice";
		int port = 50000;
		String host = "localhost";
		
		try {
			Socket client = new Socket (host, port);
			System.out.println("Connected: " + client.getRemoteSocketAddress()); 
			SecureRandom n = new SecureRandom();
			byte[] nBytes = new byte[5];
			n.nextBytes(nBytes);
			String message = id + Base64.getEncoder().encodeToString(nBytes);
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			out.println(message);
		} catch (Exception e) {
			
		}
	}

}
