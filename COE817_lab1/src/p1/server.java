package p1;

import java.net.*;
import java.io.*;
public class server {
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
	public static String chatBox (String question) {
		System.out.println("Encrypted Server Recieved: " + question);
		String chat = decrypt(question);
		System.out.println("Q: " + chat);
		if (chat.equalsIgnoreCase("Who created you?")) {
			return "I was created by Apple.";
		} else if (chat.equalsIgnoreCase("What does Siri mean?")) {
			return "Victory and Beautiful.";
		} else if (chat.equalsIgnoreCase("Are you a robot?")) {
			return "I am a virtual assistant.";
		} else {
			return "Ask me anything!";
		}
	}
	public static void main (String args[]) {
		int port = Integer.parseInt(args[0]);
		try (
				ServerSocket s = new ServerSocket(port);
				Socket c = s.accept();
				PrintWriter out = new PrintWriter(c.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
		) {
			String inputString, outputString;
			out.println("Ask me anything!");
			while ((inputString = in.readLine()) != null) {
				outputString = chatBox(inputString);
				outputString = encrypt(outputString);
				out.println(outputString);
			}
		} catch (Exception e) {
			System.out.println("Exception when connecting to port");
			System.out.println(e.getMessage());
		}
	}
}
