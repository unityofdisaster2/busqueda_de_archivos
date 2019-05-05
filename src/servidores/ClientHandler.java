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
    public ClientHandler(DataInputStream entrada, DataOutputStream salida, Socket cliente,int puerto){
        this.entrada = entrada;
        this.salida = salida;
        this.cliente = cliente;
        this.puerto = puerto;
    }

    @Override
    public void run(){
        try {
            mensajeRecibido = entrada.readUTF();
            String path;
            long tamano;
            File file = new File(Integer.toString(puerto-100)+"/"+mensajeRecibido);
            System.out.println("Hilo recibe mensaje: "+mensajeRecibido);
            path = file.getAbsolutePath();
            tamano = file.length();
            lecturaArchivos = new DataInputStream(new FileInputStream(path));
            salida.writeLong(tamano);
            salida.flush();
            byte[] b = new byte[1024];
            Long enviados = 0l;
            int porcentaje, n = 0;
            while(enviados < tamano){
                n = lecturaArchivos.read(b);
                salida.write(b,0,n);
                salida.flush();
                enviados += n;
                porcentaje = (int) (enviados * 100 / tamano);
                //System.out.print("Enviado " + porcentaje + "%\n");
            }

            salida.close();
            entrada.close();
            lecturaArchivos.close();
            cliente.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}