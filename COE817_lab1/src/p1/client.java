package p1;

import java.net.*;
import java.io.*;

public class client {
	
	public static String encrypt (String in) {
		char[] key = {'t', 'm', 'u'};
		char[] str = in.toCharArray();
		int count = 0;
		for (int i = 0; i < in.length(); i ++) {
			if (str[i] == ' ' || str[i] > 122 || str[i] < 97)
				continue;
			str[i] = (char)(key[count % 3] + str[i] - 97);
			if (str[i] >= 123) {
				str[i] -= 26;
			} 
			count ++;
		}
		return String.valueOf(str);
	}
	public static String decrypt (String in) {
		char[] key = {'t', 'm', 'u'};
		char[] str = in.toCharArray();
		int count = 0;
		for (int i = 0; i < in.length(); i ++) {
			if (str[i] == ' ' || str[i] > 122 || str[i] < 97)
				continue;
			str[i] = (char)(str[i] - key[count % 3] + 97);
			if (str[i] <= 96) {
				str[i] += 26;
			} 
			count ++;
		}
		return String.valueOf(str);
	}
	
	public static void main (String args[]) throws UnknownHostException, IOException {
		String hostName = args[0];
		int port = Integer.parseInt(args[1]);
		try (
			Socket sock = new Socket(hostName, port);
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			BufferedReader in = new BufferedReader (new InputStreamReader(sock.getInputStream()));
		){
			BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
			String servBuf;
			String cliBuf;
			servBuf = in.readLine();
			System.out.println(servBuf);
			cliBuf = buf.readLine().toLowerCase();
			if (cliBuf != null) {
				out.println(encrypt(cliBuf));
			}
			while ((servBuf = in.readLine()) != null) {
				System.out.println("Encrypted Client Recieved: " + servBuf);
				System.out.println("A: " + decrypt(servBuf));
				cliBuf = buf.readLine();
				if (cliBuf != null) {
					out.println(encrypt(cliBuf));
				}
			}
		}
		
	}
}
