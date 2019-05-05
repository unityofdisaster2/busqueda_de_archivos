 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientes;

/**
 *
 * @author sandu
 */
import controladores.FXMLVentanaPrincipalController;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;

public class FlowClient {
    private Socket cliente;
    private String host;
    private int puerto;
    private DataInputStream dis;
    private DataOutputStream dos;
    private DataOutputStream escrituraArchivo;
    private String folder;
    private FXMLVentanaPrincipalController controlador;
    
    
    /**
     * Constructor para inicializar los valores del host y puerto del servidor
     * con el que se establecera comunicacion y el nombre del folder donde
     * se realizara la descarga
     * @param host
     * @param puerto
     * @param folder 
     */
    public FlowClient(String host, int puerto, String folder) {
        this.host = host;
        this.puerto = puerto;
        this.folder = folder;
    }
    
    /**
     * Metodo para establecer el controlador de la interfaz
     * @param controlador 
     */
    public void setControlador(FXMLVentanaPrincipalController controlador){
        this.controlador = controlador;
    }
    
    /**
     * metodo encargado de conectar y guardar el stream de datos del socket
     * @return 
     */
    public boolean conectar() {
        try {
            cliente = new Socket(host, puerto);
            dis = new DataInputStream(cliente.getInputStream());
            dos = new DataOutputStream(cliente.getOutputStream());
            System.out.println("Cliente Flujo conectado...");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Metodo para desconectar socket del puerto y host actuales
     */
    public void desconectar() {
        try{
            dis.close();
            dos.close();
            cliente.close();    
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Metodo para establecer folder ligado a este cliente
     * @param folder 
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }
    
    /**
     * Metodo para realizar la descarga de un archivo en el servidor ligado
     * a los datos proporcionados en el constructor o en los setters. Si 
     * se encuentra el archivo con el nombre proporcionado se hace la descarga
     * en la carpeta ligada
     * @param nombreArchivo Recibe como argumento el nombre del archivo con
     * su extension
     */
    public void descargarArchivo(String nombreArchivo) {
        try{
            //se envia al servidor el nombre del archivo
            dos.writeUTF(nombreArchivo);
            dos.flush();
            //se obtiene del servidor el tamano del archivo
            Long tamano = dis.readLong();
            byte[] b = new byte[1024];
            //se crea un canal para escritura del archivo
            escrituraArchivo = new DataOutputStream(new FileOutputStream(folder+"/"+nombreArchivo));
            Long recibidos = 0l;
            int porcentaje, n = 0;
            //mientras los bytes recibidos sean menores la tamano del archivo se seguiran leyendo bytes
            System.out.println("cliente flujo si activa descarga de: "+nombreArchivo+" "+host+":"+puerto);
            while (recibidos < tamano) {
                
                n = dis.read(b);
                escrituraArchivo.write(b, 0, n);
                escrituraArchivo.flush();
                recibidos = recibidos + n;
                porcentaje = (int) (recibidos * 100 / tamano);
                //-------------------------------------------------------------
                controlador.controlProgress((double)porcentaje);
                //-------------------------------------------------------------
                System.out.print("Recibido: " + porcentaje + "%\n");
            }
            escrituraArchivo.close();
            System.out.println("archivo: " + nombreArchivo + " recibido");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}