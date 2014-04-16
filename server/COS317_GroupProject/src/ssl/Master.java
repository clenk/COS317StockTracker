package ssl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Master {

	public static void main(String[] args){
		System.out.println("Start Master");

			Server server = new Server();
			Thread t0 = new Thread(server);
			t0.start();

		System.out.println("End Master");
	}

	

}