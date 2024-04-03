package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class BankServer {
	static ArrayList<BankThread> threads = new ArrayList<BankThread>();
	static ArrayList<clientFile> clients = new ArrayList<clientFile>();
	
	public static ArrayList<clientFile> getClients () {
		return clients;
	}
	public static void main(String[] args) {
		int port = 50000;
		ServerSocket s;
		Socket server;
		clients.add(new clientFile("Andy|password"));
		clients.add(new clientFile("Tester|password"));
		clients.add(new clientFile("ClientC|password"));
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
