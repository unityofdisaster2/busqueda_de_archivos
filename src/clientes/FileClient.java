/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientes;

/**
 *
 * @author unityofdisaster
 */

import java.rmi.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import interfacesRMI.FileInterface;


public class FileClient {
    //private FXMLVentanaPrincipalController controlador;
    private String host;
    private int puerto;
    private String folder;
    private Registry registry;
    private FileInterface fi;
    BufferedOutputStream output;
    public FileClient(String host, int puerto, String folder) {
        this.host = host;
        this.puerto = puerto;
        this.folder = folder;
    }    

    /**
     * Metodo para establecer el controlador de la interfaz
     * @param controlador 
     */
    /*public void setControlador(FXMLVentanaPrincipalController controlador){
        this.controlador = controlador;
    }*/
    public void conectar(){
        try{
            registry = LocateRegistry.getRegistry(host,puerto);
            fi = (FileInterface) registry.lookup("rmi://"+host+":"+Integer.toString(puerto)+"/archivo");            
        }catch(RemoteException re){
            re.printStackTrace();
        }catch(NotBoundException nbe){
            nbe.printStackTrace();
        }
    }
    
    public void cerrarStream(){
        try {
            output.flush();
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public void cambiarServidor(String host, int puerto, String folder){
        this.host = host;
        this.puerto = puerto;
        this.folder = folder;
        
    }
    
    public void prepararStream(String filename){
        File file = new File(filename);
        try {
            output = new BufferedOutputStream(new FileOutputStream(folder+"/"+file.getName()));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    public void descargaArchivo(String filename,int cantidad_archivos,int num_iter){
        byte[] filedata;

        //Se crea buffer de salida donde se guardaran bytes
        try{
            filedata = fi.downloadMultiHostFile(filename,cantidad_archivos,num_iter);
            output.write(filedata,0,filedata.length);
            System.out.println("fragmento descargado");

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }catch(RemoteException re){
            re.printStackTrace();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        
    }
    
    public static void main(String [] args){
        FileClient fc = new FileClient("10.0.0.23", 9000, "9001");
        fc.conectar();
        fc.prepararStream("lisp.pdf");
        fc.descargaArchivo("lisp.pdf", 2, 1);
        fc.cambiarServidor("10.0.0.21", 9000, "9001");
        fc.conectar();
        fc.descargaArchivo("lisp.pdf", 2, 2);


        fc.cerrarStream();
        
    }
}
