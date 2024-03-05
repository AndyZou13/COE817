package p1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class KDC {

	public static void main(String[] args) {
		int port = 50000;
		ServerSocket s;
		Socket server;
		ArrayList<serverThread> threads = new ArrayList<serverThread>();
		try {
			s = new ServerSocket(port);
			server = s.accept();
			System.out.println("Connected: " + server.getRemoteSocketAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
