/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidores;

/**
 *
 * @author sandu
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * ClientHandler
 */
public class ClientHandler implements Runnable{
    DataInputStream entrada,lecturaArchivos;
    DataOutputStream salida;
    Socket cliente;
    String mensajeRecibido;
    int puerto;
    /**
     * Constructor del manejador de solicitudes de descarga de clientes
     * @param entrada Stream de datos de entrada de socket de flujo
     * @param salida Stream de salida de datos de socket de flujo
     * @param cliente Socket de flujo ligado al cliente que solicita descarga
     * @param puerto se ingresa puerto de servidor de flujo para generar carpeta
     * ligada en la que se realizara busqueda
     */
    public ClientHandler(DataInputStream entrada, DataOutputStream salida, Socket cliente,int puerto){
        this.entrada = entrada;
        this.salida = salida;
        this.cliente = cliente;
        this.puerto = puerto;
    }

    @Override
    public void run(){
        try {
            //se recibe el nombre del archivo
            mensajeRecibido = entrada.readUTF();
            String path;
            long tamano;
            File file = new File(Integer.toString(puerto-100)+"/"+mensajeRecibido);
            System.out.println("Hilo recibe mensaje: "+mensajeRecibido);
            path = file.getAbsolutePath();
            tamano = file.length();
            lecturaArchivos = new DataInputStream(new FileInputStream(path));
            //se envia a cliente el tamano del archivo
            salida.writeLong(tamano);
            salida.flush();
            byte[] b = new byte[1024];
            Long enviados = 0l;
            int porcentaje, n = 0;
            //se envia archivo en fragmentos de 1024 bytes
            while(enviados < tamano){
                n = lecturaArchivos.read(b);
                salida.write(b,0,n);
                salida.flush();
                enviados += n;
                porcentaje = (int) (enviados * 100 / tamano);
            }
            
            //se cierra conexion con stream de datos y con cliente
            salida.close();
            entrada.close();
            lecturaArchivos.close();
            cliente.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}