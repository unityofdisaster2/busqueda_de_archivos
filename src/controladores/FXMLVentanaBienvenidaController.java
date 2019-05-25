/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import pnodo.Nodo;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.ClasePrincipal;

/**
 * FXML Controller class
 *
 * @author sandu
 */
public class FXMLVentanaBienvenidaController implements Initializable {
    
    //declaracion de variables ligadas a la interfaz grafica
    @FXML
    Button btnConectar;
    @FXML
    Label lbl;
    @FXML
    TextField puerto;
    @FXML
    AnchorPane ap;
    
    //funcion ligada al boton de la interfaz
    @FXML
    public void conectar(ActionEvent evt) {
        //si el campo de puerto esta vacio se muestra un mensaje de alerta
        if (puerto.getText().equals("")) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Por favor ingrese un numero de puerto");
            a.showAndWait();

        } else {
            int pto = 0;
            try {
                pto = Integer.parseInt(puerto.getText());
                //se carga el diseno de la ventana principal
                FXMLLoader loader = new FXMLLoader();
                URL location = ClasePrincipal.class.getResource("/fxml/FXMLVentanaPrincipal.fxml");
                loader.setLocation(location);
                Parent bp;
                try {

                    bp = loader.load();
                    Stage stage = new Stage();
                    stage.setTitle("Ventana Principal: " + puerto.getText());
                    //se carga el controlador de la ventana principal
                    FXMLVentanaPrincipalController controller = loader.<FXMLVentanaPrincipalController>getController();
                    
                    
                    //se crea version inicial del nodo
                    Nodo nodo = new Nodo(pto);
                    System.out.println(nodo);
                    //se liga el nodo con el controlador
                    nodo.setControlador(controller);
                    
                    //se inicializan valores de cliente multicast
                    nodo.inicializarClienteMulticast();
                    //se ejecuta hilo de cliente multicast
                    nodo.iniciarClienteMulticast();
                    //se envia puerto al servidor multicast
                    nodo.getMc().enviarPuerto(puerto.getText(), getIpAddress());
                    //se establece direccion ip local
                    nodo.setDireccionIP(getIpAddress());
                    //se liga controlador con nodo
                    controller.setNodo(nodo);
                    //se inicializa servidor de datagrama y servidor de flujo
                    controller.postInicializacion();
                    Scene scene = new Scene(bp);
                    
                    scene.getStylesheets().add("/styles/Styles.css");
                    stage.setScene(scene);

                    //se especifica quien es la ventana padre de la nueva ventana que sera abierta
                    stage.initOwner(ap.getScene().getWindow());

                    //stage.setMaximized(true)
                    stage.setResizable(false);
                    //se cierran las ventanas que esten abiertas
                    ((Stage) bp.getScene().getWindow()).close();
                    ((Stage) ap.getScene().getWindow()).close();

                    //se agrega metodo al stage para manejar el cierre de ventana y finalizar completamente el programa
                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        public void handle(WindowEvent we) {

                            nodo.getMc().eliminarPuerto(puerto.getText());

                            System.exit(0);
                        }
                    });
                    //se muestra la ventana principal
                    stage.show();
                } catch (IOException ex) {
                    Logger.getLogger(FXMLVentanaBienvenidaController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (NumberFormatException nfe) {
                //en caso de que en el textfield del puerto se ingresen letras se manda mensaje de error
                Alert alertaPuerto = new Alert(Alert.AlertType.ERROR, "Ingrese un valor numerico");
                alertaPuerto.showAndWait();
            }

        }

    }
    
    public String getIpAddress(){
        String respuesta = "";
        try {

            DatagramSocket socket = new DatagramSocket();
            System.out.println(socket.getLocalAddress());
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            System.out.println(socket.getLocalAddress().getHostAddress());
            respuesta = socket.getLocalAddress().getHostAddress();
            socket.close();
            return respuesta;
        } catch (Exception e) {}
        return respuesta;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
