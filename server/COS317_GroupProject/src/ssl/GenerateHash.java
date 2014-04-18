package ssl;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class GenerateHash {
	
	/*
	 String input = "FoxtrotMikeLima";
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(input.getBytes("UTF-8"));
			byte[] hash = digest.digest();
			
			String hex = bytesToHex(hash);
			
			System.out.println( "hex of hash: " + hex );
			
			File f = new File("HASHPASS.txt");
			
			DataOutputStream dos = new DataOutputStream(new FileOutputStream( f ));
			
			System.out.println("hash len: " + hash.length );
		//	dos.writeInt( hash.length );
			dos.writeBytes(hex);
			dos.flush();
			dos.close();
			
			
			File f2 = new File("HASHPASS.txt");
			Scanner sc = new Scanner( f2 );
			String nuHex = sc.nextLine();
			System.out.println( "read pw hash: " + nuHex );
			System.out.println( nuHex.equals(hex) );
	 */
	
	public static void main(String[] args){
		try {
			String key = "abcdefgh12345678";
			String input = "FoxtrotMikeLima";
			
			byte[] cipherText = encrypt(key, input);
			
			
			String cipherStr = bytesToHex(cipherText);
			
			System.out.println( input );
			System.out.println( "hex(1): " + cipherStr );
			
			File f = new File("HASHPASS.txt");
			DataOutputStream dos = new DataOutputStream(new FileOutputStream( f ));
			dos.writeBytes(cipherStr);
			dos.flush();
			dos.close();
			
			
			File f2 = new File("HASHPASS.txt");
			Scanner sc = new Scanner( f2 );
			String nuHex = sc.nextLine();
			byte[] moarCipherText = hexStringToByteArray(nuHex);
			
			
			System.out.println( "hex(2): " + nuHex );
			
			String plainText = decrypt(key, moarCipherText);
			
			
			
			System.out.println( plainText );
			
			
			
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	//http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static byte[] encrypt(String key, String value)
			throws GeneralSecurityException {

		byte[] raw = key.getBytes(Charset.forName("US-ASCII"));
		if (raw.length != 16) {
			throw new IllegalArgumentException("Invalid key size.");
		}

		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec,
				new IvParameterSpec(new byte[16]));
		return cipher.doFinal(value.getBytes(Charset.forName("US-ASCII")));
	}

	public static String decrypt(String key, byte[] encrypted)
			throws GeneralSecurityException {

		byte[] raw = key.getBytes(Charset.forName("US-ASCII"));
		if (raw.length != 16) {
			throw new IllegalArgumentException("Invalid key size.");
		}
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec,
				new IvParameterSpec(new byte[16]));
		byte[] original = cipher.doFinal(encrypted);

		return new String(original, Charset.forName("US-ASCII"));
	}
}