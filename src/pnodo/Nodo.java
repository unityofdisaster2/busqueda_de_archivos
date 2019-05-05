/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnodo;

import clientes.DatagramClient;
import clientes.FlowClient;
import clientes.MulticastClient;
import controladores.FXMLVentanaPrincipalController;
import servidores.DatagramServer;
import servidores.FlowServer;

/**
 *
 * @author sandu
 */
public class Nodo {


    private MulticastClient mc;
    private FlowClient fc;
    private FlowServer fs;
    private DatagramClient dc;
    private DatagramServer ds;
    private int globalPort,puertoSiguiente,puertoAnterior;
    private String direccionIP,direccionSiguiente,direccionAnterior;
    //el id en cadena es una combinacion de ip y host. e.g. localhost:9000
    private String id;
    
    private FXMLVentanaPrincipalController controlador;
    
    public Nodo(int globalPort, String direccionIP){
        this.globalPort = globalPort;
        this.direccionIP = direccionIP;
        id = direccionIP+":"+globalPort;
    }
    public Nodo(int globalPort){
        this.globalPort = globalPort;
        this.direccionIP = "localhost";
        id = "localhost"+":"+globalPort;
    }
    
    public void setDireccionIP(String direccionIP){
        this.direccionIP = direccionIP;
    }
    
    public int getGlobalPort(){
        return this.globalPort;
    }
    
    public void setPuertoSiguiente(int puertoSiguiente){
        System.out.println("siguiente: "+puertoSiguiente);
        this.puertoSiguiente = puertoSiguiente;
    }
    
    public int getPuertoSiguiente(){
        return this.puertoSiguiente;
    }

    public void setPuertoAnterior(int puertoAnterior){
        System.out.println("anterior: "+puertoAnterior);
        this.puertoAnterior = puertoAnterior;
    }
    
    public int getPuertoAnterior(){
        return this.puertoAnterior;
    }
    
    public void setDireccionSiguiente(String direccionSiguiente){
        this.direccionSiguiente = direccionSiguiente;
    }

    public String getDireccionSiguiente(){
        return this.direccionSiguiente;
    }    

    public void setDireccionAnterior(String direccionAnterior){
        this.direccionAnterior = direccionAnterior;
    }

    public String getDireccionAnterior(){
        return this.direccionAnterior;
    }    


    /**
     * @return the mc
     */
    public MulticastClient getMc() {
        return mc;
    }

    /**
     * @return the fc
     */
    public FlowClient getFc() {
        return fc;
    }

    /**
     * @return the dc
     */
    public DatagramClient getDc() {
        return dc;
    }
    
    public void setControlador(FXMLVentanaPrincipalController controlador){
        this.controlador = controlador;
    }
    
    
    
    /*
    public void setNodoSiguiente(Nodo siguiente){
        this.siguiente = siguiente;
    }
    
    public Nodo getNodoSiguiente(Nodo siguiente){
        return this.siguiente;
    }*/
    
    public void inicializarClienteMulticast(){
        mc = new MulticastClient();
        getMc().setControlador(controlador);
    }
    
    public void inicializarClientes(){
        //no se agrega puerto ya que por default se asigna el 9999

        //cliente de datagrama debe contar con direccion del siguiente nodo para consultar
        dc = new DatagramClient(puertoSiguiente, direccionSiguiente);
        getDc().setControlador(controlador);
        
    }
    
    public void iniciarSocketFlujo(String direccionFlujo, int puertoFlujo){
        fc = new FlowClient(direccionFlujo, puertoFlujo, Integer.toString(globalPort));
        fc.setControlador(controlador);
        fc.conectar();
        
    }
    
    
    public void desconectarSocketFlujo(){
        getFc().desconectar();
    }
    
    public void peticionSocketFlujo(String filename){
        fc.descargarArchivo(filename);
    }
    
    
    
    public void inicializarServidores(){
        ds = new DatagramServer(this.globalPort);
        ds.setHostLocal(direccionIP);
        ds.setControl(controlador);
        ds.setDc(dc);
        dc.setDs(ds);
        dc.setHostLocal(direccionIP);
        fs = new FlowServer(this.globalPort+100);
    }
    
    public void iniciarClienteMulticast(){
        getMc().conectar();
        Thread s1 = new Thread(getMc());
        s1.start();
    }
    
    
    
    public void iniciarHilosServidores(){
        Thread s2 = new Thread(ds);
        Thread s3 = new Thread(fs);
        s2.start();
        s3.start();
    }
    
    public void conectarClientes(){
        dc.conectar();
        dc.setHostLocal(this.direccionIP);
    }
    
    public void desconectarClienteDatagramas(){
        dc.desconectar();
    }
    
    @Override
    public String toString(){
        return direccionIP+":"+globalPort;
    }
    
    public void actualizarPuertos(){
        dc.setPtoSiguiente(puertoSiguiente);
        dc.setHostSiguiente(direccionSiguiente);
        ds.setPtoSiguiente(puertoSiguiente);
        ds.setHostSiguiente(direccionSiguiente);
        ds.setPtoAnterior(puertoAnterior);
        ds.setHostAnterior(direccionAnterior);
    }
}
