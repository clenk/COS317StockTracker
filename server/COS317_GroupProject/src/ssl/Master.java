package ssl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Master {
	public static void main(String[] args){
		System.out.println("MASTER: Launching Server");
			Server server = new Server();
			Thread t0 = new Thread(server);
			t0.start();	
		System.out.println("MASTER: Server Thread Launched.");
	}
}