/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clientgui;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author moriarty
 */
public class FXMLDocumentController implements Initializable {
    
    private final String key = "SemperVigilisEst";
    public final int port = 8010;
    
    public Socket socket;
    public DataInputStream dis;
    public DataOutputStream dos;
    
    private String[] suffix = { "", ".CBT", ".CME", ".NYB", ".CMX", ".NYM", ".OB", ".PK", "BA",
        ".VI", ".AX", ".BR", ".SA", ".TO", ".V", ".SN", ".SS", ".SZ", ".CO", ".NX", ".PA",
        ".BE", ".BM", ".DU", ".F", ".HM", ".HA", ".MU", ".SG", ".DE", ".HK", ".BO", ".NS",
        ".JK", ".TA", ".MI", ".MX", ".AS", ".NZ", ".OL", ".LS", ".SI", ".KS", ".KQ", ".BC",
        ".BI", ".MF", ".MC", ".MA", ".ST", ".SW", ".TWO", ".TW", ".L"
    };

    //  list views
    @FXML
    public ListView listView_01_fxid;
    public ListView listView_02_fxid;
    public ListView listView_03_fxid;
    
    //  add stock stuff
    @FXML
    public TextField AddStockTF_fxid;
    @FXML
    public ComboBox AddStockCB_fxid;
    @FXML
    public Button AddStockBtn_fxid;
    @FXML
    public Label AddStockLabel_fxid;
    
    //  connection stuff
    @FXML
    public Button connect_btn;
    public Button disconnect_btn;
    
    @FXML
    public TextField netAddr_fxid;
    public TextField username_fxid;
    @FXML
    public PasswordField password_fxid;
    
    @FXML
    public Label connectLabel_fxid ;
    
    //  delete/reset stock stuff
    @FXML
    public Button refresh_btn_fxid;
    public Button del_stock_btn;
    public Button reset_stock_btn;
    
    @FXML
    public Label DelResetLabel_fxid;
    
    //  User/Pass stuff
   @FXML
   public Button resetPw_fxid;
   @FXML
   public PasswordField curPassword_fxid;
   public PasswordField newPassword_fxid;
   public PasswordField newPassword2_fxid;
   @FXML
   public Label resetPwLabel_fxid;
    
    @FXML 
    protected void AddStockBtn_action(ActionEvent event) throws GeneralSecurityException {   
        if( socket != null && socket.isConnected() ){
            
            String data = AddStockTF_fxid.getText().toUpperCase();
            data = data.replaceAll(",", "");
            data = data.replaceAll(" ", "");
            
            if( data.isEmpty() || data == null ){
                AddStockLabel_fxid.setText("Invalid data format");
                return;
            }
            
            if( AddStockCB_fxid.getSelectionModel().getSelectedIndex() > 0 ){
                data += suffix[ AddStockCB_fxid.getSelectionModel().getSelectedIndex() ];
            }
            
            try {
                String msg = ( "200,, " + data );
                byte[] msgCrypt = encrypt(key, msg);
                dos.writeInt(msgCrypt.length);
                dos.write(msgCrypt);
            } catch (IOException ex) {
                System.out.println("Controller:addStockBtn exception");
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            AddStockLabel_fxid.setText("Not connected to server");
        }
        
    }
    
    @FXML 
    protected void connectBtn_action(ActionEvent event) {
        connectLabel_fxid.setText("Connecting..."); 
            try {
                String addr = netAddr_fxid.getText().replaceAll(",", "");
                String user = username_fxid.getText().replaceAll(",", "");
                String pass = password_fxid.getText().replaceAll(",", "");
                
                if( addr == null || user == null || pass == null ||
                        addr.isEmpty() || user.isEmpty() || pass.isEmpty() ){
                    connectLabel_fxid.setText("Fill out all fields");
                    return;
                }
                System.out.println("Connecting to IP address: "+addr+" port "+port);
                socket = new Socket( addr, port);
                dos = new DataOutputStream(socket.getOutputStream() );
                dis = new DataInputStream(socket.getInputStream() );
                
                InboundListener listener = new InboundListener(this, dis, dos);
                Thread t20 = new Thread(listener);
                t20.start();
                
 
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(pass.getBytes("UTF-8"));
                byte[] hash = digest.digest();
                String hexHash = bytesToHex(hash);
                
                String hello = "100,, " + user + ",, " + hexHash;   
                byte[] helloCrypt = encrypt(key, hello);
                dos.writeInt(helloCrypt.length);
                dos.write(helloCrypt);

                //if( socket.isConnected()) connectLabel_fxid.setText("Connected.");
                
            } catch (IOException ex) {
                System.out.println("Failed to connect to localhost");
                connectLabel_fxid.setText("Failed to connect");
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GeneralSecurityException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //  helper method for the above connect() method
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
    
    @FXML 
    protected void disconnectBtn_action(ActionEvent event) {
        if( socket != null || socket.isConnected() ){
            try {
                String goodbye = "500";
                
                byte[] msgCrypt = encrypt(key, goodbye);
                dos.writeInt(msgCrypt.length);
                dos.write(msgCrypt);
                 
                socket.close();
                connectLabel_fxid.setText("Disconnected.");
//                listView_01_fxid.getItems().clear();
//                listView_02_fxid.getItems().clear();
//                listView_03_fxid.getItems().clear();
                
            } catch (IOException ex) {
                System.out.println("Didn't disconnect,(since wasn't connected before)");
                connectLabel_fxid.setText("Disconnected");
              Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GeneralSecurityException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            connectLabel_fxid.setText("Please connect");
        }
    }
    
    @FXML 
    protected void refreshBtn_action(ActionEvent event) {   
        refreshStocks();
    }
    
    private void refreshStocks(){
        if( socket != null && socket.isConnected() ){
            String msg = "220";
            try {
                byte[] msgCrypt = encrypt(key, msg);
                dos.writeInt(msgCrypt.length);
                dos.write(msgCrypt);
                //DelResetLabel_fxid.setText("Refresh stocks.");
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        DelResetLabel_fxid.setText("Refresh stocks.");
                    }
                });
            } catch (IOException ex) {
                DelResetLabel_fxid.setText("Error sending 220.");
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GeneralSecurityException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            DelResetLabel_fxid.setText("Please connect");
        }
    }
    
    @FXML 
    protected void deleteBtn_action(ActionEvent event) {
       if( socket != null && socket.isConnected() ){
            try {
                int index = listView_01_fxid.getSelectionModel().getSelectedIndex();
                String symb = (String) listView_01_fxid.getItems().get( index );      
                String msg = "450,, " + symb;
                byte[] msgCrypt = encrypt(key, msg);
                dos.writeInt(msgCrypt.length);
                dos.write(msgCrypt);
                
                DelResetLabel_fxid.setText("Delete Stock ");
                refreshStocks();
            } catch (IOException ex) {
                DelResetLabel_fxid.setText("Delete Stock exception.");
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GeneralSecurityException ex) {
               Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            DelResetLabel_fxid.setText("Please connect");
        }
    }
    @FXML
    protected void resetPwBtn_action(ActionEvent event) {
        if( socket != null && socket.isConnected() ){
            try {
                
                String curPassword = curPassword_fxid.getText();
                String newPassword1 = newPassword_fxid.getText();
                String newPassword2 = newPassword2_fxid.getText();
                if(curPassword.contains(",") || newPassword1.contains(",") || newPassword2.contains(",")){
                    resetPwLabel_fxid.setText("No commas in the passwords, please");
                    return;
                }

                
                if( curPassword.isEmpty() || newPassword1.isEmpty() || newPassword2.isEmpty() ){
                    resetPwLabel_fxid.setText("Fill out all fields");
                    return;
                }
                
                if( !newPassword1.equals(newPassword2) ) {
                    resetPwLabel_fxid.setText("New Passwords don't match!");
                    return;
                }
                
                
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                
                digest.update(curPassword.getBytes("UTF-8"));
                byte[] hash0 = digest.digest();
                String hexHashCurPW = bytesToHex(hash0);
                
                digest.update(newPassword1.getBytes("UTF-8"));
                byte[] hash = digest.digest();
                String hexHashPW1 = bytesToHex(hash);
                
                digest.update(newPassword2.getBytes("UTF-8"));
                byte[] hash2 = digest.digest();
                String hexHashPW2 = bytesToHex(hash2);
                
                System.out.println("Resetting password");
                
                String reset = "600,, " + hexHashCurPW + ",, " + hexHashPW1 + ",, " + hexHashPW2;   
                byte[] resetCrypt = encrypt(key, reset);
                dos.writeInt(resetCrypt.length);
                dos.write(resetCrypt);

                resetPwLabel_fxid.setText("Successfullly reset password");
            } catch (IOException ex) {
                resetPwLabel_fxid.setText("Password reset exception.");
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GeneralSecurityException ex) {
               Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            resetPwLabel_fxid.setText("Please connect");
        }
    }
    @FXML 
    protected void resetBtn_action(ActionEvent event) {
        if( socket != null && socket.isConnected() ){
            try {
                int index = listView_01_fxid.getSelectionModel().getSelectedIndex();
                String symb = (String) listView_01_fxid.getItems().get( index );      
                String msg = "400,, " + symb;
                byte[] msgCrypt = encrypt(key, msg);
                dos.writeInt(msgCrypt.length);
                dos.write(msgCrypt);
                
                DelResetLabel_fxid.setText("Reset Stock ");
                refreshStocks();
            } catch (IOException ex) {
                DelResetLabel_fxid.setText("Reset Stock exception.");
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GeneralSecurityException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            DelResetLabel_fxid.setText("Please connect");
        }
    }
    
    @FXML 
    protected void test_action(ActionEvent event) {
        String s1 = (String) AddStockCB_fxid.getItems().get( AddStockCB_fxid.getSelectionModel().getSelectedIndex() );
        String s2 = suffix[ AddStockCB_fxid.getSelectionModel().getSelectedIndex() ];
        
        AddStockLabel_fxid.setText( s1 + " " + s2 );
    }
    
    
    /**
     * inits the combo box for the markets. Symbols array is a
     * parallel data structure to the combo box
     */
    private void addMarkets(){
        AddStockCB_fxid.getItems().clear();
        
        AddStockCB_fxid.getItems().add( "US Market(no append)" );
        AddStockCB_fxid.getItems().add( "Chicago Board of Trade" );
        AddStockCB_fxid.getItems().add( "Chicago Mercantile Exchange" );
        AddStockCB_fxid.getItems().add( "New York Board of Trade" );
        AddStockCB_fxid.getItems().add( "New York Commodities Exchange" );
        AddStockCB_fxid.getItems().add( "New York Mercantile Exchange" );
        AddStockCB_fxid.getItems().add( "OTC Bulletin Board Market" );
        AddStockCB_fxid.getItems().add( "Pink Sheets" );
        AddStockCB_fxid.getItems().add( "Buenos Aires Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Vienna Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Australian Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Brussels Stocks" );
        AddStockCB_fxid.getItems().add( "BOVESPA-Sao Paolo Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Toronto Stock Exchange" );
        AddStockCB_fxid.getItems().add( "TSX Venture Exchange" );
        AddStockCB_fxid.getItems().add( "Santiago Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Shanghai Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Shenzhen Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Copenhagen Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Euronext" );
        AddStockCB_fxid.getItems().add( "Paris Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Berlin Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Bremen Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Dusseldorf Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Frankfurt Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Hamburg Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Hanover Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Munich Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Stuttgart Stock Exchange" );
        AddStockCB_fxid.getItems().add( "XETRA Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Hong Kong Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Bombay Stock Exchange" );
        AddStockCB_fxid.getItems().add( "National Stock Exchange of India" );
        AddStockCB_fxid.getItems().add( "Jakarta Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Tel Aviv Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Milan Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Mexico Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Amsterdam Stock Exchange" );
        AddStockCB_fxid.getItems().add( "New Zealand Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Oslo Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Lisbon Stocks" );
        AddStockCB_fxid.getItems().add( "Singapore Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Korea Stock Exchange" );
        AddStockCB_fxid.getItems().add( "KOSDAQ" );
        AddStockCB_fxid.getItems().add( "Barcelona Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Bilbao Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Madrid Fixed Income Market" );
        AddStockCB_fxid.getItems().add( "Madrid SE C.A.T.S." );
        AddStockCB_fxid.getItems().add( "Madrid Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Stockholm Stock Exchange" );
        AddStockCB_fxid.getItems().add( "Swiss Exchange" );
        AddStockCB_fxid.getItems().add( "Taiwan OTC Exchange" );
        AddStockCB_fxid.getItems().add( "Taiwan  Stock Exchange" );
        AddStockCB_fxid.getItems().add( "London Stock Exchange" );
        
        
        AddStockCB_fxid.getSelectionModel().selectFirst();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addMarkets();  
        setListener();
        password_fxid.setText( "FoxtrotMikeLima" );
    }    
    
    public void setListener(){
        listView_01_fxid.getSelectionModel().selectedItemProperty().addListener(list_1_listener);
    }
    
    public void removeListener(){
        listView_01_fxid.getSelectionModel().selectedItemProperty().removeListener(list_1_listener);
       
    }
    
    /*
     System.out.println( "Start: " + listView_01_fxid.getItems().size());
        for( int ii = listView_01_fxid.getItems().size(); ii > 0 ; ii-- ){
            System.out.println( ii + " " + listView_01_fxid.getItems().size());
            listView_01_fxid.getItems().remove(ii-1);
            
        }
        System.out.println( "Final: " + listView_01_fxid.getItems().size());
    */

    ChangeListener list_1_listener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                try {
                    int index = listView_01_fxid.getSelectionModel().getSelectedIndex();
                    String symb = (String) listView_01_fxid.getItems().get( index );
             
                    System.out.println("CLIENT:SELECT stock : " + symb);

                    String request = "300,, " + symb;
                     byte[] msgCrypt = encrypt(key, request);
                    dos.writeInt(msgCrypt.length);
                    dos.write(msgCrypt);      
                } catch (IOException ex) {
                    connectLabel_fxid.setText("Error disconnecting");
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (GeneralSecurityException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    
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
    
    public class InboundListener implements Runnable{
        
        final FXMLDocumentController pointer;
        public DataInputStream dis;
        public DataOutputStream dos;
        
        private final String key = "SemperVigilisEst";
    
        public InboundListener( FXMLDocumentController inPointer,
                 DataInputStream indis,
                     DataOutputStream indos ){
            pointer = inPointer;
            dis = indis;
            dos = indos;
        }

        @Override
        public void run() {
            try {
                int failures = 0;
                while( true ){
                    try{
                        int len = dis.readInt();
			if( len > 0 ){
                            byte[] msg = new byte[len];
                            dis.readFully(msg, 0, len);
					
                            String data = decrypt( key, msg );
                            System.out.println( "CLIENT: " + data );
                            parseData( data );
                        }else if( len == -1){
                            break;
                        }
                    } catch (Exception e){
                        System.out.println("CLIENT failed to read data from server");
                        System.out.println("CLIENT failure " + ++failures);
                        e.printStackTrace();
                    }
                    
                    if( failures > 20 ){
                        socket.close();
			Thread.currentThread().join();
                    }
                }
            } catch (IOException ex) {
                System.out.println("CLIENT I/O failure");
                //    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                    System.out.println("Failure to close InboundListener thread");
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }    
        
        private void parseData( String data ){
            final String[] sa = data.split(",, ");
            
            if( sa[0].equals("-1") ){
                System.out.println("Disconnect");
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        pointer.connectLabel_fxid.setText("Disconnected");
                   }
               });
            }else if( sa[0].equals("101") ){
                System.out.println("CLIENT: authorized");
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        pointer.connectLabel_fxid.setText("Connected-Authorized");
                        refreshStocks();
                   }
               });
                
            }else if( sa[0].equals("102") ){
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                    System.out.println("CLIENT: closing socket");
                    pointer.connectLabel_fxid.setText(sa[2]);
                    try {
                        pointer.socket.close();
                        Thread.currentThread().join();
                    } catch (IOException ex) {
                        System.out.println("Error closing socket on a 102");
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        System.out.println("Failed to stop thread on a 102");
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                });
            }else if( sa[0].equals("201") ){
                //System.out.println(sa[0]+", "+sa[1]+", "+sa[2]);
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        pointer.AddStockLabel_fxid.setText( sa[2] );
                    }
                });
                refreshStocks();
            }else if( sa[0].equals("202") ){
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        pointer.AddStockLabel_fxid.setText( sa[2] );
                    }
                });
            }else if( sa[0].equals("221") ){
                System.out.println( ">>" + data );
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        removeListener();
                        pointer.listView_01_fxid.getItems().clear(); //  clear the list first
                        for(int ii = 1; ii < sa.length; ii++){
                            System.out.println(ii + " " + sa[ii]);
                            pointer.listView_01_fxid.getItems().add(sa[ii]);
                        }
                        setListener();
                        pointer.listView_01_fxid.getSelectionModel().selectFirst();
                        System.out.println("End CLIENT:221");
                    }
                });             
            }else if( sa[0].equals("222") ){
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        DelResetLabel_fxid.setText("No stocks being tracked");
                    }
                });
            }else if( sa[0].equals("301") ){
                 Platform.runLater(new Runnable() {
                    @Override public void run() {
                        
                        String[] stockData = sa[1].split(",");
                        ArrayList<String> first24 = new ArrayList<String>();
                        ArrayList<String> post24 = new ArrayList<String>();
                        
                        String firstDate = null,
                                cutoff = null;
                        boolean postTwentyFour = false;
                        
                        pointer.listView_02_fxid.getItems().clear();    //  clear listviews 2 and 3 (24 & post24)
                        pointer.listView_03_fxid.getItems().clear();
                        
                        pointer.listView_02_fxid.setEditable(false);    //  users shalt not select these lists!
                        pointer.listView_03_fxid.setEditable(false);
                        
                        for(int ii = 0; ii < stockData.length; ii++){
                            String tmp = stockData[ii].trim();
                            
                            if( !tmp.isEmpty() ){
                                
                                if( ii == 0 ){
                                    firstDate = tmp.substring(tmp.indexOf(" "), tmp.length()).trim();
                                    String[] one = tmp.split(" ");
                                    int day = Integer.parseInt(one[2]);
                                    one[2] = "" + ++day;
                                    
                                    cutoff = one[1] + " " + one[2] + " " + one[3];
                                    
                                //    System.out.println( ">>> " + firstDate );
                                //    System.out.println( ">>> " + cutoff );
                                }
                                if( tmp.contains(cutoff) ) postTwentyFour = true;
                                
                                if( postTwentyFour ){
                                    //  update post-24
                                    if( tmp.contains(":00:00") ){   // update on the hour XX:00:00
                                    pointer.listView_03_fxid.getItems().add(tmp);
                                    }
                                }else{
                                    //  update pre-24
                                    pointer.listView_02_fxid.getItems().add(tmp);                   
                                }
                            //    System.out.println(ii + " :" + tmp + ":" );
                            }
                        }
                        DelResetLabel_fxid.setText(""); //  clear potential error message clutter
                    }
                });
            }else if( sa[0].equals("302") ){
                 Platform.runLater(new Runnable() {
                    @Override public void run() {
                        DelResetLabel_fxid.setText( sa[1] );
                        //  clear listviews 2 and 3 (24 & post24)
                        pointer.listView_02_fxid.getItems().clear();    
                        pointer.listView_03_fxid.getItems().clear();
                    }
                });
             }else if( sa[0].equals("401") ){
                 Platform.runLater(new Runnable() {
                    @Override public void run() {
                        DelResetLabel_fxid.setText( "Reset " + sa[1] + " data" );
                        //  clear listviews 2 and 3 (24 & post24)
                        pointer.listView_02_fxid.getItems().clear();    
                        pointer.listView_03_fxid.getItems().clear();
                    }
                });
             }else if( sa[0].equals("402") ){
                 Platform.runLater(new Runnable() {
                    @Override public void run() {
                        DelResetLabel_fxid.setText( "Something went wrong. Report to C-DADS immediately");
                        //  clear listviews 2 and 3 (24 & post24)
                        pointer.listView_02_fxid.getItems().clear();    
                        pointer.listView_03_fxid.getItems().clear();
                    }
                });
             }else if( sa[0].equals("451") ){
                 Platform.runLater(new Runnable() {
                    @Override public void run() {
                        DelResetLabel_fxid.setText( sa[1] );
                        pointer.listView_02_fxid.getItems().remove(sa[1]);
                        //  clear listviews 2 and 3 (24 & post24)
                        pointer.listView_02_fxid.getItems().clear();    
                        pointer.listView_03_fxid.getItems().clear();
                    }
                });
             }else if( sa[0].equals("452") ){
                 Platform.runLater(new Runnable() {
                    @Override public void run() {
                        DelResetLabel_fxid.setText( sa[1] );
                        //  clear listviews 2 and 3 (24 & post24)
                        pointer.listView_02_fxid.getItems().clear();    
                        pointer.listView_03_fxid.getItems().clear();
                    }
                }); 
             }else if( sa[0].equals("601") ){
                 Platform.runLater(new Runnable() {
                    @Override public void run() {
                        resetPwLabel_fxid.setText( "Password Reset Successful" );
                        curPassword_fxid.clear();
                        newPassword_fxid.clear();
                        newPassword2_fxid.clear();
                    }
                });
                 
             }else if( sa[0].equals("602") ){
                 Platform.runLater(new Runnable() {
                    @Override public void run() {
                        resetPwLabel_fxid.setText( "Password Reset Unsuccessful" );
                    }
                });     
            }else{
                System.out.println( "CLIENT: unknown op code: " + data );
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
}