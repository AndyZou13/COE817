package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class BankServer {
	static ArrayList<BankThread> threads = new ArrayList<BankThread>();
	public static void main(String[] args) {
		int port = 50000;
		ServerSocket s;
		Socket server;
		try {
			s = new ServerSocket(port);
			while (true) {
				server = s.accept();
				BankThread thread = new BankThread(server);
				threads.add(thread);
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
