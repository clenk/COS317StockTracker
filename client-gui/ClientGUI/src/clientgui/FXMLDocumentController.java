/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clientgui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;

/**
 *
 * @author moriarty
 */
public class FXMLDocumentController implements Initializable {
    
    private String[] suffix = { ".CBT", ".CME", ".NYB", ".CMX", ".NYM", ".OB", ".PK", "BA",
        ".VI", ".AX", ".BR", ".SA", ".TO", ".V", ".SN", ".SS", ".SZ", ".CO", ".NX", ".PA",
        ".BE", ".BM", ".DU", ".F", ".HM", ".HA", ".MU", ".SG", ".DE", ".HK", ".BO", ".NS",
        ".JK", ".TA", ".MI", ".MX", ".AS", ".NZ", ".OL", ".LS", ".SI", ".KS", ".KQ", ".BC",
        ".BI", ".MF", ".MC", ".MA", ".ST", ".SW", ".TWO", "TW", ".L"
    };

    //  Scroll panes
    @FXML
    public ScrollPane SYMB_scrollPane_fxid;
    public ScrollPane past24_scrollPane_fxid;
    public ScrollPane post24_scrollPane_fxid;
    
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
    public Label connectLabel_fxid ;
    
    //  delete/reset stock stuff
    @FXML
    public Button del_stock_btn;
    public Button reset_stock_btn;
    
    @FXML
    public Label DelResetLabel_fxid;
    
    
    @FXML 
    protected void AddStockBtn_action(ActionEvent event) {
        AddStockLabel_fxid.setText("Add symb button pressed");
    }
    
    @FXML 
    protected void connectBtn_action(ActionEvent event) {
        connectLabel_fxid.setText("Connecting...");
    }
    
    @FXML 
    protected void disconnectBtn_action(ActionEvent event) {
        connectLabel_fxid.setText("Disconnected.");
    }
    
    @FXML 
    protected void deleteBtn_action(ActionEvent event) {
        DelResetLabel_fxid.setText("Delete Stock.");
    }
    
    @FXML 
    protected void resetBtn_action(ActionEvent event) {
        DelResetLabel_fxid.setText("Reset Stock.");
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
        // TODO
        
        addMarkets();
    }    
    
}
