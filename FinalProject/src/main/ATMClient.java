package main;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ATMClient {

	private static Socket client;
	private static int port = 50000;
	private static String host = "localhost";
	private static String[] username = {"admin1", "admin2", "admin3"};
	private static String[] password = {"test", "pass", "admin"};
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	
	private static void logIn() {
		boolean loggedIn = false;
		while (!loggedIn) {
			Scanner input = new Scanner(System.in);
			System.out.println("Please enter your username: ");
			String u = input.nextLine();
			System.out.println("Please enter your password: ");
			String p = input.nextLine();
			for (String n : username) {
				if (n.compareTo(u) == 0) {
					for (String m : password) {
						if (m.compareTo(p) == 0) {
							loggedIn = true;
						}
					}
				}
			}
			if (loggedIn != true)
				System.out.println("*Credentials Invalid*");
		}
	}
	public static void main(String[] args) {
		try {
			client = new Socket(host, port);
			System.out.println("Connected: " + client.getRemoteSocketAddress());
			logIn();
			out = new ObjectOutputStream(client.getOutputStream());
			in = new ObjectInputStream(client.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
