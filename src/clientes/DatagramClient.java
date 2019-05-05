/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientes;

import controladores.FXMLVentanaPrincipalController;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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

    public String preguntarArchivo(String filename){
        try {
            //strBytes = filename.getBytes();

            //primero se envia host del cliente que consulta
            envios = new DatagramPacket(hostLocal.getBytes(),hostLocal.length(),hostSiguiente,ptoSiguiente);
            cl.send(envios);
            
            envios = new DatagramPacket(filename.getBytes(),filename.length(),hostSiguiente,ptoSiguiente);
            cl.send(envios);
            
            controlador.addMensaje("se pregunta por existencia de archivo "+filename+" a: "+hostSiguiente+":"+Integer.toString(ptoSiguiente));
            recepciones = new DatagramPacket(new byte[200],200);
            
            //se recibe respuesta del servidor
            cl.receive(recepciones);
            System.out.println("se ha recibido algo en cliente datagrama: "+new String(recepciones.getData(),0,recepciones.getLength()));
            return new String(recepciones.getData(),0,recepciones.getLength());
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }    
}