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
    
    public FlowClient(String host, int puerto, String folder) {
        this.host = host;
        this.puerto = puerto;
        this.folder = folder;
    }
    
    public void setControlador(FXMLVentanaPrincipalController controlador){
        this.controlador = controlador;
    }

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

    public void desconectar() {
        try{
            dis.close();
            dos.close();
            cliente.close();    
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

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
            //-------------------------------------------------------------
            //controlador.clearProgress();
            //-------------------------------------------------------------
            System.out.println("archivo: " + nombreArchivo + " recibido");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}