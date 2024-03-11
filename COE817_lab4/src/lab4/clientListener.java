package lab4;

import java.io.ObjectInputStream;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class clientListener extends Thread{

	private ObjectInputStream in;
	private PublicKey pub;
	private byte[] encryptedIn;
	private byte[] decrypted;
	private Cipher ciph;
	private String sharedKey;
	private SecretKey sec;
	
	public clientListener(ObjectInputStream in, String s) {
		this.in = in;
		this.sharedKey = s;
	}
	
	@Override
	public void run(){
		while (true) {
			try {
				pub = (PublicKey) in.readObject();
				encryptedIn = (byte[]) in.readObject();
				byte[] p1 = Arrays.copyOfRange(encryptedIn, 0, encryptedIn.length - 256);
				byte[] p2= Arrays.copyOfRange(encryptedIn, encryptedIn.length - 256, encryptedIn.length);
				
				SecretKeyFactory fact = SecretKeyFactory.getInstance("DES");
				byte[] b = sharedKey.getBytes();
				sec = fact.generateSecret(new DESKeySpec(b));
				
				ciph = Cipher.getInstance("DES/ECB/PKCS5Padding");
				ciph.init(Cipher.DECRYPT_MODE, sec);
				decrypted = ciph.doFinal(p1);
				
				Signature unsign = Signature.getInstance("SHA256withRSA");
				unsign.initVerify(pub);
				unsign.update(decrypted);
				if (unsign.verify(p2) == false) {
					System.out.println("Invalid Signature");
					return;
				}
				System.out.println("Decrypted: " + new String(decrypted));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
