package main;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class ATMClient {

	private static Socket client;
	private static int port = 50000;
	private static String host = "localhost";
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	private static String nonce;
	private static String atmID = "atmA";
	private static byte[] encrypted;
	private static byte[] decrypted;
	private static byte[] encryptedIn;
	
	private static Cipher ciph;
	private static String sharedKey = "BankKey817";
	private static SecretKey sec;
	private static SecretKey tempSec;
	private static SecretKey masterSec;
	private static String masterString;
	
	private static SecretKey encryptionKey;
	private static String macKey;
	
	private static void generateNonce () {
		SecureRandom n = new SecureRandom();
		byte[] nBytes = new byte[5];
		n.nextBytes(nBytes);
		nonce = Base64.getEncoder().encodeToString(nBytes);
	}
	
	private static SecretKey generateSecret(String key) {
		try { 
			SecretKeyFactory fact = SecretKeyFactory.getInstance("DES");
			byte[] b = key.getBytes();
			return fact.generateSecret(new DESKeySpec(b));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void encodeDES (String message, SecretKey s) {
		try {
			ciph = Cipher.getInstance("DES/ECB/PKCS5Padding");
			ciph.init(Cipher.ENCRYPT_MODE, s);
			encrypted = ciph.doFinal(message.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void encodeDES (byte[] message, SecretKey s) {
		try {
			ciph = Cipher.getInstance("DES/ECB/PKCS5Padding");
			ciph.init(Cipher.ENCRYPT_MODE, s);
			encrypted = ciph.doFinal(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void decodeDES (byte[] message, SecretKey s) {
		try {
			ciph = Cipher.getInstance("DES/ECB/PKCS5Padding");
			ciph.init(Cipher.DECRYPT_MODE, s);
			decrypted = ciph.doFinal(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void masterGenerate (String message) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] b = digest.digest(message.getBytes());
			StringBuilder hexString = new StringBuilder(2 * b.length);
		    for (int i = 0; i < b.length; i++) {
		        String hex = Integer.toHexString(0xff & b[i]);
		        if(hex.length() == 1) {
		            hexString.append('0');
		        }
		        hexString.append(hex);
		    }
		    masterString = hexString.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static String hmac(String data, String key){
		try {
		    SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
		    Mac mac = Mac.getInstance("HmacSHA256");
		    mac.init(secretKeySpec);
		    byte[] b = mac.doFinal(data.getBytes());
		    String r = "";
		    for (byte i : b) {
		    	r += String.format("%02X", i);
		    }
		    return r;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static boolean verifyMac(String[] arr) {
		if (arr[arr.length - 1].compareTo(hmac("macKey", masterString)) == 0) {
			if (new Timestamp(System.currentTimeMillis()).getTime() - Long.parseLong(arr[arr.length - 2]) < 1500)
			return true;
		}
		return false;
	}
	
	public static void main(String[] args) {
		try {
			client = new Socket (host, port);
			System.out.println("Connected: " + client.getRemoteSocketAddress());
			out = new ObjectOutputStream(client.getOutputStream());
			in = new ObjectInputStream(client.getInputStream());
			generateNonce();
			sec = generateSecret(sharedKey);
			String message = nonce + "|" + Long.toString(new Timestamp(System.currentTimeMillis()).getTime()) + "|" + atmID;
			encodeDES(message, sec);
			out.writeObject(encrypted);
			encryptedIn = (byte[])in.readObject();
			decodeDES(encryptedIn, sec);
			message = new String(decrypted);
			String[] arr = message.split("\\|");
			if (arr[0].compareTo(nonce) != 0 || Long.parseLong(arr[2]) - new Timestamp(System.currentTimeMillis()).getTime() > 20) {
				System.out.println("Message invalid, closing connection");
				client.close();
				return;
			}
			encryptedIn = (byte[])in.readObject();
			decodeDES(encryptedIn, sec);
			tempSec = new SecretKeySpec(decrypted, 0, decrypted.length, "DES"); 
			message = arr[1] + "|" + Long.toString(new Timestamp(System.currentTimeMillis()).getTime());
			encodeDES(message, tempSec);
			out.writeObject(encrypted);
			masterGenerate(nonce + arr[1]);
			masterSec = generateSecret(masterString);
			encodeDES(masterString, masterSec);
			out.writeObject(encrypted);
			encryptedIn = (byte[]) in.readObject();
			decodeDES(encryptedIn, masterSec);
			if (masterString.compareTo(new String(decrypted)) != 0) {
				System.out.println("Master String invalid, closing connection");
				client.close();
				return;
			}
			encryptionKey = generateSecret(hmac("encryption", masterString));
			macKey = hmac("macKey", masterString);
			
			Scanner input = new Scanner(System.in);
			boolean loggedIn = false;
			System.out.println("***** Welcome to the COE817 ATM *****");
			while (loggedIn == false) {
				System.out.println("Please enter your username:");
				String user = input.nextLine();
				System.out.println("Please enter your password:");
				String pass = input.nextLine();
				message = user + "|" + pass + "|" + new Timestamp(System.currentTimeMillis()).getTime() + "|" + macKey;
				encodeDES(message, encryptionKey);
				out.writeObject(encrypted);
				encryptedIn = (byte[]) in.readObject();
				decodeDES(encryptedIn, encryptionKey);
				String[] ar = new String(decrypted).split("\\|");
				if (verifyMac(ar) == false) {
					System.out.println("Invalid message detected, closing connection");
					client.close();
					return;
				}
				if ("Invalid Login".compareTo(new String(ar[0])) != 0) {
					loggedIn = true;
					System.out.println("***" + ar[0] + "***");
					break;
				}
				System.out.println("***" + ar[0] + "***");
				
			}
			
			while (true) {
				System.out.println("Please choose from the actions below by submitting a number (1, 2, 3, 4):");
				System.out.println("1: Deposit");
				System.out.println("2: Withdrawl");
				System.out.println("3: Check Balance");
				System.out.println("4: Log out");
				int option = Integer.parseInt(input.nextLine());
				double amt;
				String m;
				switch (option) {
					case 1:
						System.out.println("How much do you want to deposit:");
						amt = Double.parseDouble(input.nextLine());
						m = "1" + "|" + Double.toString(amt) + "|" + new Timestamp(System.currentTimeMillis()).getTime() + "|" + macKey;
						encodeDES(m, encryptionKey);
						out.writeObject(encrypted);
						break;
					case 2:
						System.out.println("How much do you want to withdraw:");
						amt = Double.parseDouble(input.nextLine());
						m = "2" + "|" + Double.toString(amt) + "|" + new Timestamp(System.currentTimeMillis()).getTime() + "|" + macKey;
						encodeDES(m, encryptionKey);
						out.writeObject(encrypted);
						break;
					case 3:
						m = "3" + "|" + new Timestamp(System.currentTimeMillis()).getTime() + "|" + macKey;
						encodeDES(m, encryptionKey);
						out.writeObject(encrypted);
						encryptedIn = (byte[]) in.readObject();
						decodeDES(encryptedIn, encryptionKey);
						String[] a = new String(decrypted).split("\\|");
						if (verifyMac(a) == false) {
							System.out.println("Invalid message detected, closing connection");
							client.close();
							return;
						}
						System.out.println("Current Balance: $" + a[0]);
						break;
					case 4:
						System.out.println("Thank you for choosing COE817 ATM");
						client.close();
						return;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
