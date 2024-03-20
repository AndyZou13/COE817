package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BankThread extends Thread {
	private Socket sock;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	

	public BankThread (Socket s) {
		this.sock = s;
		try {
			out = new ObjectOutputStream(sock.getOutputStream());
			in = new ObjectInputStream(sock.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			System.out.println("Connected: " + sock.getRemoteSocketAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
