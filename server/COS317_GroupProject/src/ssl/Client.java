/*
 * @(#)SSLSocketClientWithClientAuth.java	1.5 01/05/10
 *
 * Copyright 1994-2004 Oracle and/or its affiliates. All rights reserved. 
 *
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met: 
 * 
 * -Redistribution of source code must retain the above copyright 
 * notice, this list of conditions and the following disclaimer.
 * 
 * Redistribution in binary form must reproduce the above copyright 
 * notice, this list of conditions and the following disclaimer in 
 * the documentation and/or other materials provided with the 
 * distribution. 
 * 
 * Neither the name of Oracle and/or its affiliates. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY 
 * EXCLUDED. Oracle and/or its affiliates. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT 
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS 
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER 
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed, licensed or 
 * intended for use in the design, construction, operation or 
 * maintenance of any nuclear facility. 
 */


package ssl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client implements Runnable {

	private SocketFactory factory = (SocketFactory) SocketFactory.getDefault();
	private Socket clientSocket;
	
	private DataOutputStream outToServer;
	private BufferedReader inFromServer;
	
	private boolean connected = false;

	public Client(){

	}


	public void connect(){
		try {
			//	InetAddress addr;
			clientSocket = (Socket) factory.createSocket("localhost", 717);
			

			outToServer = new DataOutputStream(
					clientSocket.getOutputStream());
			inFromServer = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));


			
			String modifiedSentance = inFromServer.readLine();
			System.out.println("CLIENT: " + modifiedSentance);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect(){
		try {
			inFromServer.close();
			outToServer.close();
			clientSocket.close();
			connected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void send( String data ){
		try {
			outToServer.writeBytes( data + '\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		connect();
		
	//	waitForData();
	}


	private void waitForData() {
		while( connected ){
			try {
				String data = inFromServer.readLine();
			} catch (IOException e) {
				System.out.println("Failed to read data from server");
				e.printStackTrace();
			}
		}
		
	}	
}