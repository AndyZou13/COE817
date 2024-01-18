package p2;

import java.io.IOException;
import java.net.*;

public class multiServer {

	public static void main(String[] args) throws IOException {
		int port = Integer.parseInt(args[0]);
		boolean list = true;
		try (ServerSocket sock = new ServerSocket(port)) {
			while (list) {
				new serverThread(sock.accept()).start();
			}
		} catch (Exception e) {
			System.out.println("Could not listen to port");
			System.exit(-1);
		}
	}

}
