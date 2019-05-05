/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import pnodo.Nodo;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author sandu
 */
public class FXMLVentanaPrincipalController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    ListView<String> servidores;
    @FXML
    TextArea mensajes;
    @FXML
    TextField nombreArchivo;
    @FXML
    Button buscar;
    @FXML
    ProgressBar progreso;
    @FXML
    ProgressIndicator indProgreso;
    private LinkedList<String> listaMensajes;
    private ObservableList<String> elementosServidores;
    private Nodo nodo;
    
    //controla el estado de las barras de progresoo
    public void controlProgress(double valor) {
        progreso.setProgress(valor);
        indProgreso.setProgress(valor);
    }
    
    //reestablece las barras de progreso al valor inicial
    public void clearProgress() {
        progreso.setProgress(0.0);
        indProgreso.setProgress(0.0);
    }

    public void setNodo(Nodo nodo) {
        this.nodo = nodo;
    }

    public Nodo getNodo() {
        return this.nodo;
    }

    @FXML
    public void buscarArchivo(ActionEvent evt) {
        if (nombreArchivo.getText().equals("")) {
            //si no hay nada en el textbox se envia mensaje de error
            Alert a = new Alert(Alert.AlertType.ERROR, "Ingrese algun nombre en el campo");
            a.showAndWait();
        } else {
            Alert b;
            //se cambia bandera del servidor para indicar que a este no se le debe preguntar por el archivo
            nodo.getDs().switchFlag();
            String respuesta = nodo.getDc().preguntarArchivo(nombreArchivo.getText());
            System.out.println("respuesta del servidor" + respuesta);
            if (respuesta.equals("-1")) {
                b = new Alert(Alert.AlertType.INFORMATION, "No se ha encontrado el archivo");
                b.showAndWait();

            } else if (!respuesta.equals("")) {
                b = new Alert(Alert.AlertType.CONFIRMATION, "Archivo encontrado en: " + respuesta + " presione aceptar para comenzar descarga");
                b.showAndWait();
                //se inicializan valores del socket de flujo y se conecta
                nodo.iniciarSocketFlujo(respuesta.substring(0, respuesta.indexOf(":")), Integer.parseInt(respuesta.substring(respuesta.indexOf(":") + 1, respuesta.length())));
                //se hace la peticion por el archivo solicitado
                nodo.peticionSocketFlujo(nombreArchivo.getText());
                //dado que pueden cambiar los valores en un futuro se cierra socket de flujo
                nodo.desconectarSocketFlujo();
                b = new Alert(Alert.AlertType.CONFIRMATION, "Archivo descargado");
                b.showAndWait();
                clearProgress();
            }
        }
    }
    
    /**
     * Metodo para actualizar los valores desplegados en la lista de servidores
     * @param lista 
     */
    @FXML
    public void actualizarLista(LinkedList<String> lista) {
        servidores.getItems().clear();
        servidores.refresh();
        elementosServidores = FXCollections.observableList(lista);
        try {
            servidores.setItems(elementosServidores);
        } catch (Exception e) {
        }
        servidores.refresh();

    }
    
    
    /**
     * Metodo para iniciar ejecucion de clientes y servidores
     */
    public void postInicializacion() {
        nodo.inicializarClienteDatagrama();
        nodo.inicializarServidores();
        nodo.conectarClienteDatagrama();
        nodo.iniciarHilosServidores();
    }
    
    /**
     * Metodo para agregar mensajes desplegados por las distintas acciones
     * realizadas por el programa
     * @param mensaje 
     */
    @FXML
    public void addMensaje(String mensaje) {
        listaMensajes.add(mensaje);
        listaMensajes.add("");
        String cadenaMensajes = "";
        int contador = 1;
        for (String msjs : listaMensajes) {
            if (!msjs.equals("")) {
                cadenaMensajes += "(" + Integer.toString(contador) + ") " + msjs + "\n";
                contador++;
            }else{
                cadenaMensajes += msjs + "\n";
            }

        }
        mensajes.setText(cadenaMensajes);
    }
    
    /**
     * Funcion que funciona como constructor de la interfaz grafica
     * @param url
     * @param rb 
     */
    @Override
    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        nombreArchivo.setPromptText("e.g. archivo.pdf");
        listaMensajes = new LinkedList<>();

        mensajes.setEditable(false);

    }

}
