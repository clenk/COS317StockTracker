import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.StrongTextEncryptor;

public class EncryptModule {

	public static boolean checkFile(String in, boolean shouldExist) throws IOException {
		if (in.endsWith(".txt")) { // Make sure the output/input filename ends in .txt
			if (shouldExist) { // if input file, then make sure it actually exists
				try {
					BufferedReader test = new BufferedReader(new FileReader(in));
					test.close();
					return true;
				} catch(FileNotFoundException f) {
					System.out.println("File not found!");
					return false;
				}
			}
			else
				return true;
		}
		else {
			System.out.println("File must end in .txt");
		}
		return false; 
	}


	public static boolean checkInput (String in, boolean whichCrypt) {
		if (whichCrypt) {
			if (in.equals("E")||in.equals("D")){
				return true;
			}
			else {
				System.out.println("Error: Must enter either an (E)ncrypt or (D)ecrypt");
				return false;
			}
		}
		else {
			if (in.equals("S")||in.equals("F")){
				return true;
			}
			else {
				System.out.println("Error: Must enter either a (S)tring or (F)ile");
				return false;
			}
		}
	}


	public static void cryptFile(String type, String crypt, String inFile, String outFile) throws IOException {
		String line = "";
		Scanner sc;
		if (type.equals("F")) {
			sc = new Scanner(new FileReader(inFile));
			BufferedWriter output = new BufferedWriter(new FileWriter(outFile));
			boolean done = false;
			while (!done) {
				line = sc.nextLine();
				BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
				textEncryptor.setPassword("change this");
				String myEncryptedText="";
				if(crypt.equals("E")) {
					myEncryptedText = textEncryptor.encrypt(line);
				}
				else {
					myEncryptedText = textEncryptor.decrypt(line);
				}
				try {
					output.write(myEncryptedText);
				} catch ( IOException e ) {
					e.printStackTrace();
				}
				if (!sc.hasNextLine()) {
					done = true;
				}
			}
			output.close();
			sc.close();
		}
		else {
			BufferedWriter output = new BufferedWriter(new FileWriter(outFile));
			line = inFile;
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword("change this");
			String myEncryptedText="";
			if(crypt.equals("E")) {
				myEncryptedText = textEncryptor.encrypt(line);
			}
			else {
				myEncryptedText = textEncryptor.decrypt(line);
			}
			try {
				output.write(myEncryptedText);
			} catch ( IOException e ) {
				e.printStackTrace();
			}
			output.close();
		}
	}

	public static String getInputFile(Scanner input) throws IOException {
		String inputFile ="";
		System.out.println("Please enter the name of a file to encrypt/decypt:");
		while (!checkFile(inputFile = input.next(), true)) {
			System.out.println("Please enter a valid filename");
		}
		return inputFile;
	}

	public static String getOutputFile(Scanner input) throws IOException {
		String outputFile="";
		System.out.println("Please enter the name of the output file:");
		while (!checkFile(outputFile = input.next(), false)) {
			System.out.println("Please enter a valid filename");
		}
		return outputFile;
	}

	public static String getCrypt(Scanner input) throws IOException {
		String whichCrypt ="";
		System.out.println("(E)ncrypt or (D)ecrypt?");
		while(!checkInput(whichCrypt=input.next(),true)) {
			System.out.println("(E)ncrypt or (D)ecrypt?");
		}
		return whichCrypt;
	}

	public static String getInputString(Scanner input) throws IOException {
		String inputString ="";
		String userInput ="";
		System.out.println("Please enter the String to encrypt/decrypt. ");
		System.out.println("Then enter '\\stop'.");
		while(!userInput.equalsIgnoreCase("\\stop")){
			inputString = inputString + userInput;
			userInput = input.nextLine();
		}
		return inputString;
	}

	public static String getType(Scanner input) throws IOException {
		String whichType="";
		System.out.println("Encrypting a (S)tring or (F)ile?");
		while(!checkInput(whichType=input.next(),false)) {
			System.out.println("Encrypting/Decrypting a (S)tring or (F)ile?");
		}
		return whichType;
	}

	public static void main(String[] args) throws IOException {
		Scanner input = new Scanner(System.in);
		String whichType=""; // Holds whether user wants to read from a file or input a string.
		String inputFile=""; // Holds filename/string to read from.
		String outputFile=""; // Holds filename to write to.
		String whichCrypt=""; // Holds if user wants to encrypt or decrypt
		whichType = getType(input); // Choose string or file to read from.
		if (whichType.equals("F"))
			inputFile = getInputFile(input);
		else 
			inputFile = getInputString(input);
		outputFile = getOutputFile(input);
		whichCrypt = getCrypt(input);
		cryptFile(whichType,whichCrypt,inputFile, outputFile);
		input.close();
		if (whichCrypt.equals("E"))
			System.out.println("Successfully encrypted: "+inputFile+"\nOutput File: "+outputFile);
		else 
			System.out.println("Successfully decrypted: "+inputFile+"\nOutput File: "+outputFile);
	}
}
