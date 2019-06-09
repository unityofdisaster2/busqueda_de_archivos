/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import pnodo.Nodo;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
    @FXML
    Label lblSiguiente;
    @FXML 
    Label lblAnterior;
    
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
            nodo.getDs().turnOnFlag();
            //String respuesta = nodo.getDc().preguntarArchivo(nombreArchivo.getText());
            LinkedHashMap<String,LinkedList<String>> respuesta = nodo.getDc().preguntarArchivo(nombreArchivo.getText(),new LinkedHashMap<String,LinkedList<String>>());
            

            if (respuesta.isEmpty()) {
                b = new Alert(Alert.AlertType.INFORMATION, "No se ha encontrado el archivo");
                addMensaje("archivo no encontrado en ningun nodo");
                b.showAndWait();

            } else if (!respuesta.equals("")) {
                //b = new Alert(Alert.AlertType.CONFIRMATION, "Archivo encontrado en: " + respuesta + " presione aceptar para comenzar descarga");
                addMensaje("archivo encontrado");
                b = new Alert(Alert.AlertType.CONFIRMATION, "Archivo encontrado ");
                b.showAndWait();

                new vistaDescargas(nodo, respuesta,nombreArchivo.getText()).setVisible(true);
                

            }
            nodo.getDs().turnOffFlag();
        }
    }
    
    public void actualizarEtiquetas(int et_anterior, int et_siguiente){
        String ant,sig;
        ant = Integer.toString(et_anterior);
        sig = Integer.toString(et_siguiente);
        Platform.runLater(() -> {
            if(ant.equals("0") && sig.equals("0")){
                lblAnterior.setText("");
                lblSiguiente.setText("");
                
            }else{
                lblAnterior.setText("nodo anterior: "+ant);
                lblSiguiente.setText("nodo siguiente: "+sig);
            }
        });
    }

    /**
     * Metodo para actualizar los valores desplegados en la lista de servidores
     *
     * @param lista
     */
    @FXML
    public void actualizarLista(LinkedList<String> lista) {
        Platform.runLater(() -> {
            elementosServidores = FXCollections.observableList(lista);
            servidores.setItems(elementosServidores);
            servidores.refresh();
        });

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
     *
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
            } else {
                cadenaMensajes += msjs + "\n";
            }

        }
        mensajes.setText(cadenaMensajes);
    }

    /**
     * Funcion que funciona como constructor de la interfaz grafica
     *
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
