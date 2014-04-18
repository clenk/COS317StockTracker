package ssl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class Server implements Runnable{

	private final int port = 8010;
	private ServerSocket serverSocket;
	private ServerSocketFactory factory;
	public StockReader reader;

	public final String username = "hgruber";


	public Server(){
		System.out.println("Starting server");
		//	get the reader spawned off in it's own thread
		reader = new StockReader();
		Thread t1 = new Thread( reader );
		t1.start();
	}

	/**
	 * This method creates a socket and listens
	 */
	public void run() {
		try {
			factory = ServerSocketFactory.getDefault();
			serverSocket = (ServerSocket) factory.createServerSocket(port);


			while(true){
				Socket welcome = serverSocket.accept();
				welcome.setSoLinger(true, 180000);
				System.out.println("SERVER: Client connected.");

				MessageParser mp = new MessageParser(welcome, this);
				Thread t10 = new Thread(mp);
				t10.start();
			}

		} catch (IOException e) {
			System.out.println("Failed to create server socket");
			e.printStackTrace();
		}
	}
}

class MessageParser implements Runnable {

	private final String key = "SemperVigilisEst";

	Server server;
	Socket socket;

	DataInputStream inFromClient;
	DataOutputStream outToClient;

	public MessageParser( Socket inSocket, Server inServer ){
		socket = inSocket;
		server = inServer;

		try {
			inFromClient = new DataInputStream( socket.getInputStream() );
			outToClient = new DataOutputStream(socket.getOutputStream() );
		} catch (IOException e) {
			System.out.println("MESSAGEPARSER: failed to create I/O from socket");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		int failures = 0;
		while( true ){
			try {

				int len = inFromClient.readInt();
				if( len > 0 ){
					byte[] msg = new byte[len];
					inFromClient.readFully(msg, 0, len);

					String clientSentance = decrypt( key, msg );
					System.out.println("SERVER: " + clientSentance );
					parseMsgData( clientSentance );
				}
			} catch (IOException e) {
				System.out.println("MESSAGEPARSER: failed " + ++failures);
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}

			System.out.println( "Failures: " + failures );
			if( failures > 20 ){

				try {
					socket.close();
					Thread.currentThread().join();
				} catch (IOException e) {
					System.out.println("I/O problem after severed connection.");
					e.printStackTrace();
				} catch (InterruptedException e) {
					System.out.println("Problem closing thread after severed connection.");
					e.printStackTrace();
				}


			}
		}

	}

	private void sendMsg( String data ){
		try {

			if( data.equals("500") || data.startsWith("102,, NOAUTH") ){
				outToClient.write(-1);
			}
			
			byte[] encrypted = encrypt(key, data);
			outToClient.writeInt(encrypted.length);
			outToClient.write(encrypted);

		} catch (IOException e) {
			System.out.println("SERVER:MESSAGEPARSER: failed to send data: " + data);
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	private void parseMsgData(String clientSentance) {

		String[] sa = clientSentance.split(",, ");


		if( sa[0].equals("100") ){
			System.out.println("Parsing login auth for " + sa[1] + ":" + sa[2] );

			if( sa[1].equals(server.username) && 
					sa[2].equals(server.reader.getPassword()) ){
				System.out.println("SERVER: AUTH USER");
				String response = "101,, AUTH";
				sendMsg(response);

			}else{
				System.out.println("SERVER: NOAUTH");
				String response = "102,, NOAUTH, Credentials not valid";
				try {
					sendMsg(response);
					socket.close();
					Thread.currentThread().join();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}


		}else if( sa[0].equals("200") ){

			int ret = server.reader.addStockSymbol(sa[1]);

			if( ret == -2 ){
				sendMsg( "202, FALSE,, Stock " + sa[1] + " does not exist");
				System.out.println("SERVER: sending 202-0");
			}
			else if( ret == -1 ){
				sendMsg( "202,, FALSE, Failed to add" );
				System.out.println("SERVER: sending 202-1");
			}
			else if( ret == 0 ){
				sendMsg( "202,, FALSE,, " + sa[1] +" already exists" );
				System.out.println("SERVER: sending 202-2");
			}
			else if( ret == 1 ){
				sendMsg( "201,, TRUE,, " + sa[1] +" added");
				System.out.println("SERVER: sending 201");
			}

		}else if( sa[0].equals("220") ){
			System.out.print("Get list of stocks");

			String[] symbols = server.reader.getStockSymbols();
			if( symbols == null ){
				String response = "222";

				sendMsg(response);
				System.out.println(" -- No stock data");

			}else{
				String response = "221,, ";
				for( int ii = 0; ii < symbols.length; ii++ ){
					if( ii != symbols.length-1) response += symbols[ii] + ",, ";
					else response += symbols[ii];
				}

				sendMsg(response);
				System.out.println(" -- symbols exist");
				System.out.println( ":" + response + ":" );

			}

		}else if( sa[0].equals("300") ){


			System.out.println( "Request stock data for " + sa[1] );
			String data = server.reader.getStockData(sa[1]);
			String response = "";
			if( data.isEmpty() ){	//then no stock being tracked
				response += "302,, No data on file for " + sa[1];
			}else{

				response += "301,, " + data;
			}

			sendMsg(response);


		}else if( sa[0].equals("400") ){

			System.out.println( "Reset stock data for " + sa[1] );

			boolean tf = server.reader.resetStockData( sa[1] );
			System.out.println("SERVER: " + tf );

			String response = null;
			if( tf ){
				response = "401,, " + sa[1];
			}else{
				response = "402,, " + sa[1];
			}

			sendMsg(response);


		}else if( sa[0].equals("450") ){

			System.out.println( "Delete stock data for " + sa[1] );

			boolean tf = server.reader.deleteStockData( sa[1] );
			System.out.println("SERVER: " + tf );

			String response = null;
			if( tf ){
				response = "451,, " + sa[1] + " successfully deleted";
			}else{
				response = "452,, " + sa[1] + " was unable to be deleted";
			}

			sendMsg(response);

		}else if( sa[0].equals("500") ){
			System.out.println("SERVER: 500");
			try {
				String response = "500";
				sendMsg(response);
				socket.close();
				Thread.currentThread().join();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else if( sa[0].equals("600") ){
			System.out.println("SERVER: 600");
			
			boolean tf = false;
			if( sa[1].equals(server.reader.getPassword()) && sa[2].equals(sa[3]) ){
				tf = true;

				try {
					File dir = new File("");
					String path = dir.getAbsolutePath() + "\\";
					dir = new File(path);
					File f = new File( dir.getAbsolutePath() + "\\HASHPASS.txt");
					FileWriter fw;
					fw = new FileWriter( f.getAbsolutePath() );
					BufferedWriter br = new BufferedWriter( fw );
					br.write(sa[2] + "\n"); 
					
					fw.flush();
					br.flush();
					fw.close();
					br.close();	
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			String response = "";
			if( tf ){
				response = "601";
			}else{
				response = "602";
			}
			
			sendMsg(response);
		}else{
			System.out.println("SERVER: unknown msg type: " + clientSentance);
		}
	}

	public byte[] encrypt(String key, String value)
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

	public String decrypt(String key, byte[] encrypted)
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
