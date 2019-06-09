/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidores;

/**
 *
 * @author unityofdisaster
 */
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import interfacesRMI.FileInterface;


public class FileServer implements Runnable{
    private Registry registry;
    private FileImpl objetoImplementacion;
    private FileInterface stub;
    private int puerto;
    private String folderName;
    
    public FileServer(int puerto){
        this.puerto = puerto;
    }
    
    //funcion para obtener explicitamente la direccion IP del equipo 
    //necesario para que equipos externos se conecten
    public String getIpAddress(){
        String respuesta = "";
        try {
            //se abre un socket de datagrama y se conecta a una direccion por defecto
            DatagramSocket socket = new DatagramSocket();
            System.out.println(socket.getLocalAddress());
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            //Se muestra la ip ligada
            System.out.println(socket.getLocalAddress().getHostAddress());
            //se guarda la ip ligada en respuesta
            respuesta = socket.getLocalAddress().getHostAddress();
            socket.close();
            return respuesta;
        } catch (Exception e) {}
        return respuesta;
    }
    
    @Override
    public void run(){
        try{
            //para que funcione correctamente se deben asignar dos propiedades
            //la primera es la ruta del servidor de nombres (creo)
            //la segunda es la ip de la maquina para que otras se puedan comunicar con ella
            System.setProperty("java.rmi.server.codebase", "file:/home/unityofdisaster/archivo");
            //System.setProperty("java.rmi.server.hostname","10.0.0.23");
            String direccionIP = getIpAddress();
            System.setProperty("java.rmi.server.hostname",direccionIP);
            
            //se conecta al puerto ligado a este nodo 
            registry = java.rmi.registry.LocateRegistry.createRegistry(puerto);
            System.out.println("RMI registro listo");
            
            //se crea objeto donde estan implementados los metodos de la interfaz
            objetoImplementacion = new FileImpl(Integer.toString(puerto-100));
            //se crea el stub 
            stub = (FileInterface) UnicastRemoteObject.exportObject(objetoImplementacion, 0);
            //se hace un religado de la ruta donde quedan definidas las operaciones de archivos
            registry.rebind("rmi://"+direccionIP+":"+Integer.toString(puerto)+"/archivo", stub);
            System.err.println("Servidor listo");
            
        }catch(RemoteException re){
            System.out.println("Servidor de archivo RMI: " + re.getMessage());
            re.printStackTrace();
        }

        
    }   
    /*
    public static void main(String [] args) throws InterruptedException{
        FileServer fs = new FileServer(9000);
        Thread t1 = new Thread(fs);
        t1.start();
        t1.join();
    }*/
    



}
