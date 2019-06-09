/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientes;

import controladores.FXMLVentanaPrincipalController;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import servidores.DatagramServer;

/**
 *
 * @author sandu
 */
public class DatagramClient {
    private DatagramSocket cl;
    private DatagramPacket envios;
    private DatagramPacket recepciones;
    private int ptoSiguiente;
    private String respuesta;
    private InetAddress hostSiguiente;
    private byte[] strBytes;
    private boolean flag;
    private String hostLocal;
    private DatagramServer ds;
    private FXMLVentanaPrincipalController controlador;
    
    
    /**
     * Constructor de cliente de datagrama, recibe como argumento 
     * el puerto y host del servidor al que apunta.
     * @param ptoSiguiente 
     * @param hostSiguiente 
     */
    public DatagramClient(int ptoSiguiente,String hostSiguiente){
        try {
            this.ptoSiguiente = ptoSiguiente;
            this.hostSiguiente = InetAddress.getByName(hostSiguiente);
            this.respuesta = "";
            flag = false;
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Se recibe como parametro controlador de la interfaz grafica
     * @param controlador 
     */
    public void setControlador(FXMLVentanaPrincipalController controlador){
        this.controlador = controlador;
    }
    
    
    public void setHostLocal(String hostLocal){
        this.hostLocal = hostLocal;
    }
    
    public void setPtoSiguiente(int ptoSiguiente){
        this.ptoSiguiente = ptoSiguiente;
    }
    
    public int getPtoSiguiente(){
        return this.ptoSiguiente;
    }
    
    /**
     * Setter para servidor de datagrama
     * @param ds 
     */
    public void setDs(DatagramServer ds){
        this.ds = ds;
    }
   
    

    
    public void setHostSiguiente(String host){
        try {
            this.hostSiguiente = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            Logger.getLogger(DatagramClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    /**
     * Metodo para conectar cliente de datagramas (se deja que el constructor
     * ligue al cliente a un puerto por default)
     * @return 
     */
    public boolean conectar(){
        try {
            //para esta aplicacion no sera necesario especificar puerto de cliente
            cl = new DatagramSocket();
            System.out.println("Cliente datagramas conectado...");
            return true;
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    public void desconectar(){
        cl.close();
        cl.disconnect();
    }
    
    public void switchFlag(){
        flag = true;
    }
    
    /**
     * Metodo encargado de preguntar al servidor del siguiente nodo
     * si cuenta con el archivo solicitado. 
     * @param filename Se recibe como parametro el nombre con extension del
     * archivo que se desea buscar
     * @return se retorna la respuesta del servidor que puede ser:
     * -1: No se encontro el archivo
     * "host:puerto": datos del nodo donde se encontro el archivo para preparar
     * la descarga
     */
    public String preguntarArchivo(String filename){
        try {
            //strBytes = filename.getBytes();

            //primero se envia host del cliente que consulta
            envios = new DatagramPacket(hostLocal.getBytes(),hostLocal.length(),hostSiguiente,ptoSiguiente);
            cl.send(envios);
            
            //se despliega mensaje en interfaz grafica donde se notifica que se comenzo la busqueda
            controlador.addMensaje("se pregunta por existencia de archivo "+filename+" a: "+hostSiguiente+":"+Integer.toString(ptoSiguiente));
            //posteriormente se envia nombre del archivo
            envios = new DatagramPacket(filename.getBytes(),filename.length(),hostSiguiente,ptoSiguiente);
            cl.send(envios);
            
            
            recepciones = new DatagramPacket(new byte[200],200);
            
            //se recibe respuesta del servidor
            cl.receive(recepciones);
            
            //se retornala cadena que contiene la respuesta del servidor
            String encontrado = new String(recepciones.getData(),0,recepciones.getLength());
            if(!encontrado.equals("-1")){
                controlador.addMensaje("archivo encontrado en: "+encontrado);
            }
            return new String(recepciones.getData(),0,recepciones.getLength());
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }
    
    
    
    
    public LinkedHashMap<String,LinkedList<String>> preguntarArchivo(String filename, LinkedHashMap<String,LinkedList<String>> mapaLocal){
        try {
            //strBytes = filename.getBytes();

            //primero se envia host del cliente que consulta
            envios = new DatagramPacket(hostLocal.getBytes(),hostLocal.length(),hostSiguiente,ptoSiguiente);
            cl.send(envios);
            
            //se despliega mensaje en interfaz grafica donde se notifica que se comenzo la busqueda
            
            controlador.addMensaje("se pregunta por existencia de archivo "+filename+" a: "+hostSiguiente+":"+Integer.toString(ptoSiguiente));
            
            
            
            //posteriormente se envia nombre del archivo
            envios = new DatagramPacket(filename.getBytes(),filename.length(),hostSiguiente,ptoSiguiente);
            cl.send(envios);
            
            //**********   envio de estructura de datos ******************************
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(mapaLocal);
            byte [] lst = baos.toByteArray();
            
            envios = new DatagramPacket(lst,lst.length,hostSiguiente,ptoSiguiente);
            cl.send(envios);
            //**********   envio de estructura de datos ******************************
            
            //************ recepcion de estructura de datos **************************
            recepciones = new DatagramPacket(new byte[1024*4],1024*4);
            //se recibe respuesta del servidor

            cl.receive(recepciones);
            
            //se recibe un arreglo de bytes que corresponde a un LinkedHashMap
            ByteArrayInputStream bais = new ByteArrayInputStream(recepciones.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            LinkedHashMap<String,LinkedList<String>> mapaAux = new LinkedHashMap<>();
            try{
                mapaAux = (LinkedHashMap<String,LinkedList<String>>)ois.readObject();
            }catch(Exception e){e.printStackTrace();}
            
            
            //************ recepcion de estructura de datos **************************
            
            if(mapaAux.size() == 0){
                System.out.println("do something");
            }
            return mapaAux;
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }    
     
}