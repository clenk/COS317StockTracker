package ssl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class StockReader implements Runnable{

	private File[] fileList;
	private final String key = "XvDIim9yVWPCc3Nj";
	private String password;

	private boolean symbolsExist = false,
				hashpass = false;

	public StockReader(){
		buildFileList_TXT();
	}

	private void buildFileList_TXT() {
		File dir = new File("");
		String path = dir.getAbsolutePath() + "\\";
		dir = new File(path);

		if( dir.isDirectory() ){

			fileList = dir.listFiles();
			for( File f : fileList ){
				if( f.getName().contains(".txt") ){
					System.out.println( f.isFile() + " " + f.getAbsolutePath() );
				}
				if( f.getName().equals("symbols.txt") )
					symbolsExist = true;
				if( f.getName().equals("HASHPASS.txt") )
					hashpass = true;
			}
			
			if( !hashpass ){
				try {
					File f = new File( dir.getAbsolutePath() + "\\HASHPASS.txt");
					FileWriter fw = new FileWriter( f.getAbsolutePath() );
					BufferedWriter br = new BufferedWriter( fw );
					String whome = "FoxtrotMikeLima";
					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					digest.update(whome.getBytes("UTF-8"));
					byte[] nonotme = digest.digest();
					String yougotme = bytesToHex(nonotme);
					br.write(yougotme + "\n"); 
					password = yougotme;
					
					fw.flush();
					br.flush();
					fw.close();
					br.close();	
					symbolsExist = true;
				} catch (IOException e) {
					System.out.println("Failure to create file: HASHPASS.txt ");
					e.printStackTrace();
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
				}		
			}else{
				System.out.println("HASHPASS.txt exists.");
				// Read in the hashed password
				password = readInPassword();
				System.out.println("PASSWORD: " + password);
			}

			if( !symbolsExist ){			
				try {
					File f = new File( dir.getAbsolutePath() + "\\symbols.txt");
					FileWriter fw = new FileWriter( f.getAbsolutePath() );
					BufferedWriter br = new BufferedWriter( fw );
					fw.flush();
					br.flush();
					fw.close();
					br.close();	
					symbolsExist = true;
				} catch (IOException e) {
					System.out.println("Failure to create file: symbols.txt ");
					e.printStackTrace();
				}
			}else{
				System.out.println("symbols.txt exists.");
				//	if the symbols file does exist, then check to see that all the appropriate files exist as well

				try {
					ArrayList<String> symbols = new ArrayList<String>();
					Scanner sc = new Scanner( new File("symbols.txt") );
					while( sc.hasNextLine() ){
						symbols.add( sc.nextLine() );
					}
					sc.close();
					
					/**
					 * NOTE: adding the hex of the encrypted files is what we want
					 */

					for( String symb : symbols ){
						boolean fileCreated = false;
						for( File filenames : fileList ){
							if( filenames.getAbsolutePath().contains(symb) ){
								fileCreated = true;
							}
						}

						if( fileCreated ){
							System.out.println("File exists for " + symb );
						}else{
							System.out.println("Need to create file for " + symb );

							File f = new File( symb + ".txt");
							FileWriter fw = new FileWriter( f.getAbsolutePath() );
							BufferedWriter br = new BufferedWriter( fw );
							fw.flush();
							br.flush();
							fw.close();
							br.close();	
						}
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			System.out.println( "No DICE." );
		}
	}

	/**
	 * @param symb
	 * @return int i, -1 = failed method call, 0 = duplicate, 1 = successful add
	 */
	public int addStockSymbol( String symb ){
		try {
			if( symbolsExist ){

				Scanner sc = new Scanner( new File("symbols.txt") );
				ArrayList<String> symbols = new ArrayList<String>();
				while( sc.hasNextLine() ){
					String hex = sc.nextLine();
					byte[] cipherBytes = hexStringToByteArray(hex);
					String plaintext = decrypt(key, cipherBytes);
					symbols.add( plaintext );
				}
				sc.close();

				if( symbols.contains(symb) ){
					//	symbol exists, we have a duplicate
					return 0;
				}else{

					//	check to see if the symbol is valid against finance.yahoo.com
					String html = null, name = null;
					double price = -1.0;

					html = readHTML( symb );
					price = price(html);
					name = name(html);

					if( name == null || price == -1.0 ){
						return -2;
					}

					//	write the new symbol to the file		
					FileWriter fw = new FileWriter( new File("symbols.txt").getAbsolutePath(), true );
					BufferedWriter br = new BufferedWriter( fw );
					
					byte[] nonotme = encrypt(key, symb);
					String yougotme = bytesToHex(nonotme);
					
					br.write(yougotme);
					br.newLine();
					fw.flush();
					br.flush();
					fw.close();
					br.close();	

					//	create new file for the stock symbol
					buildFileList_TXT();	//this method call will take care of that

					return 1;
				}
			}else{
				return -1;
			}
		} catch (FileNotFoundException e) {
			return -1;
		} catch (IOException e) {
			return -1;
		} catch (GeneralSecurityException e) {
			return -1;
		}

	}

	public String[] getStockSymbols(){
		try {
			ArrayList<String> symbols = new ArrayList<String>();
			Scanner sc = new Scanner( new File("symbols.txt") );
			while( sc.hasNextLine() ){
				String hex = sc.nextLine();
				byte[] cipherBytes = hexStringToByteArray(hex);
				String plaintext = decrypt(key, cipherBytes);
				if( !plaintext.isEmpty() ) symbols.add( plaintext );
			}
			sc.close();
			
			if( symbols.size() == 0 ) return null;

			String[] ret = new String[symbols.size()];
			for( int ii = 0; ii < symbols.size(); ii++ ){
				ret[ii] = symbols.get(ii);
			}
			
			//	alphabetize the stock symbols
			Arrays.sort(ret);
			return ret;
		} catch (FileNotFoundException e) {
			return null;
		} catch (GeneralSecurityException e) {
			return null;
		}
	}

	public String getStockData( String symb ){
		try {
			buildFileList_TXT();	//	do a quick refresh
			
			//	get the hexstring of the plaintext symbol	
			byte[] cipheredsymbol = encrypt(key, symb);
			String hashedSymbol = bytesToHex(cipheredsymbol);
			
			String data = "";
			for( File file : fileList ){
				if( file.getName().contains(hashedSymbol) ){
					Scanner sc = new Scanner( file );
					while( sc.hasNextLine() ){
						String hexStr = sc.nextLine();
						byte[] cipherText = hexStringToByteArray(hexStr);
						String plaintext = decrypt(key, cipherText);

						if( plaintext != null && !plaintext.isEmpty() ) 
							data += plaintext + ", ";
					}
					sc.close();
					break;
				}
			}
			return data;
		} catch (FileNotFoundException e) {
			return null;
			//	e.printStackTrace();
		} catch (GeneralSecurityException e) {
			return null;
			//	e.printStackTrace();
		}
	}
	
	public String readInPassword() {
		try {
			String data = "";
			Scanner sc = new Scanner(new File( "HASHPASS.txt" ) );
			if( sc.hasNextLine() ){ // only need the first line
				data = sc.nextLine();
			}
			sc.close();
			return data;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public boolean resetStockData( String symb ){
		try {
			buildFileList_TXT();	//	do a quick refresh
			
			File file = new File(symb+".txt");
			FileWriter fw = new FileWriter( file );
			BufferedWriter br = new BufferedWriter( fw );
			fw.flush();
			br.flush();
			fw.close();
			br.close();	
			
			return true;
		} catch (FileNotFoundException e) {
			return false;
			//	e.printStackTrace();
		} catch (IOException e) {
			return false;
		//	e.printStackTrace();
		}
	}
	
	public boolean deleteStockData( String symb ){
		try {
			
			ArrayList<String> symbols = new ArrayList<String>();
			Scanner sc = new Scanner( new File("symbols.txt") );
			while( sc.hasNextLine() ){
				String hexStr = sc.nextLine();
				byte[] cipherText = hexStringToByteArray(hexStr);
				String plaintext = decrypt(key, cipherText);
				
				if( !plaintext.equals(symb) ) symbols.add( plaintext );
			}
			sc.close();
			
			File symbolFile = new File("symbols.txt");
			symbolFile.delete();
			
			symbolFile = new File("symbols.txt");
			
			FileWriter fw = new FileWriter( symbolFile );
			BufferedWriter br = new BufferedWriter( fw );
			for( String s : symbols ){
				byte[] nonotme = encrypt(key, s);
				String yougotme = bytesToHex(nonotme);
				br.write( yougotme + "\n" );
			}
			fw.flush();
			br.flush();
			fw.close();
			br.close();	

			File file = new File(symb+".txt");
			file.setWritable(true);
			System.gc();
			boolean delete = file.delete();
			
			
		//	buildFileList_TXT();	//	rebuild list for the global fileList variable
			
			return delete;
		} catch (FileNotFoundException e) {
			System.out.println("throw 1");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.out.println("throw 2");
			e.printStackTrace();
			return false;
		} catch (GeneralSecurityException e) {
			System.out.println("throw 3 -- crypto throw");
			e.printStackTrace();
			return false;
		}
	}
	
	
	

	@Override
	public void run() {
		long curMin = -1,
				curSec = -1;

		while(true){
			long time = System.currentTimeMillis();
			long min = ( time / (1000*60)) % 60;
			long sec = ( time/1000)%60;

			long tmpMod = -1;;

			if( curMin == -1 ) curMin = min;
			if( curSec == -1 ) curSec = sec;

			if( sec != curSec ){

				String dateStr = new Date(time).toString();

				//	System.out.println("time (s): " + curMin + "m " + curSec + "s " );
				curSec = sec;

				if( min != curMin ){
					curMin = min;
					System.out.println("time (m): " + curMin + "m " + curSec + "s " );
					System.out.println( dateStr );

					tmpMod = curMin % 5;
					if( tmpMod == 0 ){

						long hour = (time / (1000*60*60)) % 24;	//	time / (1000*60*60) % 24; 

						System.out.println("MOD TIME: " + hour + " " + curMin + "m " + curSec + "s " );
						readStocks( dateStr );
					}
				}			
			}			
		}		
	}

	private void readStocks( String date ){

		try {
			ArrayList<String> symbols = new ArrayList<String>();
			Scanner sc = new Scanner( new File("symbols.txt") );
			while( sc.hasNextLine() ){
				symbols.add( sc.nextLine() );
			}
			sc.close();

			for( String s : symbols ){

				String html = null, name = null;
				double price = -1.0;

				try{
					//	decrypt the stock symbol so we can go and get it
					byte[] cipherBytes = hexStringToByteArray(s);
					String plainSymb = decrypt(key, cipherBytes);
					
					html = readHTML( plainSymb );
					price = price(html);
					name = name(html);
					//	System.out.println( s + " :: " + name );

					//	System.out.printf("\t%s %.2f\n", s, price(html) );

					String fname = s + ".txt";
					System.out.println( s + "//" + plainSymb +" " + writeToFile( fname, s, price, date) );
				} catch (Exception e){
					System.out.println( "name: " + name + "\nprice: " + price );
				}

			}

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}



	// Given symbol, get HTML
	private String readHTML(String symbol) {
		In page = new In("http://finance.yahoo.com/q?s=" + symbol);
		String html = page.readAll();
		return html;
	}

	// Given symbol, get current stock price.
	private double price(String html) {
		try{
			int p     = html.indexOf("yfs_l84", 0);      // "yfs_l84" index
			int from  = html.indexOf(">", p);            // ">" index
			int to    = html.indexOf("</span>", from);   // "</span>" index
			String price = html.substring(from + 1, to);
			return Double.parseDouble(price.replaceAll(",", ""));

		}catch (Exception e){
			return -1.0;
		}
	}

	// Given symbol, get current stock name.
	private String name(String html) {
		try{
			int p    = html.indexOf("<title>", 0);
			int from = html.indexOf("Summary for ", p);
			int to   = html.indexOf("- Yahoo! Finance", from);
			String name = html.substring(from + 12, to);
			return name;

		}catch (Exception e){
			return null;
		}
	}



	private boolean writeToFile( String fname, String symb, double price, String date ){
		try {

			String[] date2 = date.toString().split(" ");
			date = date2[1] + " " + date2[2] + " " + date2[3];
			
			
			String toFile = price + " " + date;
			byte[] toFile_crypto = encrypt(key, toFile);
			String toFile_hex = bytesToHex(toFile_crypto);
			
			FileWriter fw = new FileWriter( fname, true );
			BufferedWriter br = new BufferedWriter( fw );
			br.write( toFile_hex );
			br.newLine();
			
			fw.flush();
			br.flush();
			fw.close();
			br.close();	

			return true;
		} catch (IOException e) {
			return false;
		} catch (GeneralSecurityException e) {
			return false;
		}
	}
	
	/**
	 * Methods below are for encrypting and decrypting data
	 */
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