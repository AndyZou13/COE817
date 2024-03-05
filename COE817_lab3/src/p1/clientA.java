package p1;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class clientA {

	public static void main(String[] args) {
		String id = "clientA";
		String host = "localhost";
		int port = 50000;
		Socket client;
		try {
			client = new Socket (host, port);
			System.out.println("Connected: " + client.getRemoteSocketAddress()); 
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
