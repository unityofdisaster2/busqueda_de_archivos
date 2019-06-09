/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnodo;

import clientes.DatagramClient;
import clientes.FileClient;
import clientes.FlowClient;
import clientes.MulticastClient;
import servidores.MulticastServer;
import controladores.FXMLVentanaPrincipalController;
import servidores.DatagramServer;
//import servidores.FlowServer;

import servidores.FileServer;
/**
 *
 * @author sandu
 */
public class Nodo {


    private MulticastClient mc;
    private MulticastServer ms;
    //private FlowClient fc;
    private FileClient fc;
    //private FlowServer fs;
    private FileServer fs;
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
    
    public String getDireccionIP(){
        return this.direccionIP;
    }
    
    public int getGlobalPort(){
        return this.globalPort;
    }
    
    public void setPuertoSiguiente(int puertoSiguiente){
        //System.out.println("siguiente: "+puertoSiguiente);
        this.puertoSiguiente = puertoSiguiente;
    }
    
    public int getPuertoSiguiente(){
        return this.puertoSiguiente;
    }

    public void setPuertoAnterior(int puertoAnterior){
        //System.out.println("anterior: "+puertoAnterior);
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
    
    public MulticastServer getMs(){
        return ms;
    }

    /**
     * @return the fc
     */
    public FileClient getFc() {
        return fc;
    }

    /**
     * @return the dc
     */
    public DatagramClient getDc() {
        return dc;
    }
    
    public DatagramServer getDs(){
        return ds;
    }
    
    public void setControlador(FXMLVentanaPrincipalController controlador){
        this.controlador = controlador;
    }
    
    

    
    /**
     * Metodo para unicamente inicializar valores importantes del cliente
     * multicast
     */
    public void inicializarMulticast(){
        mc = new MulticastClient();
        ms = new MulticastServer(this.globalPort);
        getMc().setControlador(controlador);
    }
    

    
    /**
     * Metodo para unicamente inicializar valores importantes del cliente
     * de datagramas
     */
    public void inicializarClienteDatagrama(){
        //no se agrega puerto ya que por default se asigna el 9999

        //cliente de datagrama debe contar con direccion del siguiente nodo para consultar
        dc = new DatagramClient(puertoSiguiente, direccionSiguiente);
        getDc().setControlador(controlador);
        
    }
    
    /*
    public void iniciarSocketFlujo(String direccionFlujo, int puertoFlujo){
        fc = new FlowClient(direccionFlujo, puertoFlujo, Integer.toString(globalPort));
        fc.setControlador(controlador);
        fc.conectar();
        
    }*/
    
    public void iniciarClienteArchivos(String host,int puerto){
        System.out.println("puertoooooo"+puerto);
        System.out.println("foldeeeeeerr"+globalPort);
        fc = new FileClient(host, puerto, Integer.toString(globalPort));
        fc.conectar();
    }
    
    /*
    public void desconectarSocketFlujo(){
        getFc().desconectar();
    }*/
    
    /**
     * Funcion para realizar la peticion de descarga de un archivo
     * @param filename se recibe como argumento nombre de archivo con su extension
     */
    /*
    
    public void peticionSocketFlujo(String filename){
        fc.descargarArchivo(filename);
    }*/
    
    
    /**
     * Metodo para unicamente establecer valores importantes que sean neceasrios
     * para los servidores
     */
    public void inicializarServidores(){
        ds = new DatagramServer(this.globalPort);
        ds.setHostLocal(direccionIP);
        ds.setControl(controlador);
        ds.setDc(dc);
        dc.setDs(ds);
        dc.setHostLocal(direccionIP);
        fs = new FileServer(this.globalPort+100);
    }
    
    /**
     * Metodo para ejecutar el hilo correspondiente al cliente multicast
     */
    public void conectarMulticast(){
        Thread s1 = new Thread(getMc());
        s1.start();
        Thread s2 = new Thread(getMs());
        s2.start();
    }
    

    
    
    /**
     * Metodo para ejecutar los hilos correspondientes a cliente de datagrama
     * y cliente de flujo
     */
    public void iniciarHilosServidores(){
        Thread s2 = new Thread(ds);
        Thread s3 = new Thread(fs);
        s2.start();
        s3.start();
    }
    
    /**
     * Metodo para iniciar conexion de cliente de datagramas
     */
    public void conectarClienteDatagrama(){
        dc.conectar();
        dc.setHostLocal(this.direccionIP);
    }
    
    /**
     * Metodo para finalizar conexion de cliente de datagramas
     */
    public void desconectarClienteDatagramas(){
        dc.desconectar();
    }
    
    @Override
    public String toString(){
        return direccionIP+":"+globalPort;
    }
    
    /**
     * Metodo que sera llamado cada que se actualicen los puertos ligados
     * a este nodo
     */
    public void actualizarPuertos(){
        dc.setPtoSiguiente(puertoSiguiente);
        dc.setHostSiguiente(direccionSiguiente);
        ds.setPtoSiguiente(puertoSiguiente);
        ds.setHostSiguiente(direccionSiguiente);
        ds.setPtoAnterior(puertoAnterior);
        ds.setHostAnterior(direccionAnterior);
    }
}
