package p1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class KDC {
	static ArrayList<serverThread> threads = new ArrayList<serverThread>();
	static ArrayList<String> semKeys = new ArrayList<String>(Collections.nCopies(100, ""));
	public static void main(String[] args) {
		int port = 50000;
		ServerSocket s;
		Socket server;
		try {
			s = new ServerSocket(port);
			while (true) {
				server = s.accept();
				serverThread thread = new serverThread(server);
				threads.add(thread);
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
