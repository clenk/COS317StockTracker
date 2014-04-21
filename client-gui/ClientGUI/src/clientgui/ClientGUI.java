/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clientgui;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author moriarty
 */
public class ClientGUI extends Application implements Runnable {
    private FXMLLoader fxmlLoader;
    @Override
    public void start(Stage stage) throws Exception {
        URL location = getClass().getResource("FXMLDocument.fxml");
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = (Parent) fxmlLoader.load(location.openStream());
        
        Scene scene = new Scene(root);
        
        stage.setTitle("C-DADS LLC");
        
        stage.setScene(scene);
        stage.show();
        
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
 //   public static void main(String[] args) {
 //       launch(args);
 //   }
            
    @Override
    public void run() {
       launch();
    }
    
//    @Override  
//    public void stop() {  
//        ((MemoryController) fxmlLoader.getController()).haltSimulation();  
//    } 
    
}
