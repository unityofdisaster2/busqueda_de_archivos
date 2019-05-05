/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author sandu
 */
public class ClasePrincipal extends Application {

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader();
        try {
            //se carga el modelo grafico de la interfaz bienvenida
            Parent root = loader.load(getClass().getResource("/fxml/FXMLVentanaBienvenida.fxml"));

            //se agrega elemento a 
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/styles/Styles.css");
            primaryStage.setTitle("Ventana Inicial");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException io) {
            io.printStackTrace();
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
