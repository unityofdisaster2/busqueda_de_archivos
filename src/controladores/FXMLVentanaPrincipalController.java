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
public class FXMLVentanaPrincipalController implements Initializable{



    
    /**
     * Initializes the controller class.
     */
    @FXML ListView<String> servidores;
    @FXML TextArea mensajes;
    @FXML TextField nombreArchivo;
    @FXML Button buscar;
    @FXML ProgressBar progreso;
    @FXML ProgressIndicator indProgreso;
    private LinkedList<String> listaMensajes;
    private ObservableList<String> elementosServidores;
    private Nodo nodo;
    
    public void controlProgress(double valor){
        progreso.setProgress(valor);
        indProgreso.setProgress(valor);        
    }
    
    public void clearProgress(){
        progreso.setProgress(0.0);
        indProgreso.setProgress(0.0);
    }
    
    
    public void setNodo(Nodo nodo){
        this.nodo = nodo;
    }
    
    public Nodo getNodo(){
        return this.nodo;
    }
    
    
    




    
    @FXML
    public void buscarArchivo(ActionEvent evt){
        if(nombreArchivo.getText().equals("")){
            //si no hay nada en el textbox se envia mensaje de error
            Alert a = new Alert(Alert.AlertType.ERROR,"Ingrese algun nombre en el campo");
            a.showAndWait();
        }else{
            Alert b;
            nodo.getDs().switchFlag();
            String respuesta = nodo.getDc().preguntarArchivo(nombreArchivo.getText());
            System.out.println("respuesta del servidor"+respuesta);
            if(respuesta.equals("-1")){
                b = new Alert(Alert.AlertType.INFORMATION,"No se ha encontrado el archivo");
                b.showAndWait();
                
            }else if(!respuesta.equals("")){
                b = new Alert(Alert.AlertType.CONFIRMATION,"Archivo encontrado en: "+respuesta+" presione aceptar para comenzar descarga");
                b.showAndWait();
                nodo.iniciarSocketFlujo(respuesta.substring(0, respuesta.indexOf(":")), Integer.parseInt(respuesta.substring(respuesta.indexOf(":")+1, respuesta.length())));
                nodo.peticionSocketFlujo(nombreArchivo.getText());
                nodo.desconectarSocketFlujo();
                b = new Alert(Alert.AlertType.CONFIRMATION,"Archivo descargado");
                b.showAndWait();
                clearProgress();
            }
        }
    }
    
    @FXML
    public void actualizarLista(LinkedList<String> lista){
        servidores.getItems().clear();
        servidores.refresh();
        elementosServidores = FXCollections.observableList(lista);
        try{
            servidores.setItems(elementosServidores);        
        }catch(Exception e){}
        servidores.refresh();
        
    }
    
    public void postInicializacion(){
        nodo.inicializarClientes();
        nodo.inicializarServidores();
        nodo.conectarClientes();
        nodo.iniciarHilosServidores();
    }
    
    @FXML
    public void addMensaje(String mensaje){
        listaMensajes.add(mensaje);
        listaMensajes.add("");
        String cadenaMensajes = "";
        for(String msjs: listaMensajes){
            cadenaMensajes += msjs+"\n";
        }
        mensajes.setText(cadenaMensajes);        
    }
    
    
    @Override
    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        nombreArchivo.setPromptText("e.g. archivo.pdf");
        listaMensajes = new LinkedList<>();

        //nodo.inicializarClienteMulticast();
        //nodo.iniciarClienteMulticast();
        
        mensajes.setEditable(false);

    }    


    
}
