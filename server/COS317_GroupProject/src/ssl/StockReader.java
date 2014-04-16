package ssl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class StockReader implements Runnable{

	private File[] fileList;

	private boolean symbolsExist = false;

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
					symbols.add( sc.nextLine() );
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
					br.write( symb + "\n" );
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
		}

	}

	public String[] getStockSymbols(){


		try {
			ArrayList<String> symbols = new ArrayList<String>();
			Scanner sc = new Scanner( new File("symbols.txt") );
			while( sc.hasNextLine() ){
				String tmp = sc.nextLine();
				if( !tmp.isEmpty() ) symbols.add( tmp );
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
		}
	}

	public String getStockData( String symb ){
		try {
			buildFileList_TXT();	//	do a quick refresh
			
			String data = "";
			for( File file : fileList ){
				if( file.getName().contains(symb) ){
					Scanner sc = new Scanner( file );
					while( sc.hasNextLine() ){
						String s = sc.nextLine();
						if( s != null && !s.isEmpty() ) data += s + ", ";
					}
					sc.close();
					break;
				}
			}
			return data;
		} catch (FileNotFoundException e) {
			return null;
			//	e.printStackTrace();
		}
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
				String tmp = sc.nextLine();
				if( !tmp.equals(symb) ) symbols.add( tmp );
			}
			sc.close();
			
			File symbolFile = new File("symbols.txt");
			symbolFile.delete();
			
			symbolFile = new File("symbols.txt");
			
			FileWriter fw = new FileWriter( symbolFile );
			BufferedWriter br = new BufferedWriter( fw );
			for( String s : symbols ){
				br.write( s + "\n" );
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
					html = readHTML( s );
					price = price(html);
					name = name(html);
					//	System.out.println( s + " :: " + name );

					//	System.out.printf("\t%s %.2f\n", s, price(html) );

					String fname = s + ".txt";
					System.out.println( s + " " + writeToFile( fname, s, price, date) );
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
			
			FileWriter fw = new FileWriter( fname, true );
			BufferedWriter br = new BufferedWriter( fw );
			br.write( price + " " + date +"\n" );
			
			fw.flush();
			br.flush();
			fw.close();
			br.close();	

			return true;
		} catch (IOException e) {
			return false;
		}
	}
}