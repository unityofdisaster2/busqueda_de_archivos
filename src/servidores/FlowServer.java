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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor
 */
public class FlowServer implements Runnable{
    ServerSocket servidor;
    Socket cliente;
    DataInputStream dis;
    DataOutputStream dos;
    FileInputStream fis;
    FileOutputStream fos;
    int puerto;
    String folderName;
    public FlowServer(int puerto){
        this.puerto = puerto;
    }

    @Override
    public void run(){
        try {
            servidor = new ServerSocket(puerto);
            for(;;){
                System.out.println("se llega al ciclo del servidor");
                //se espera la conexion de un cliente
                cliente = servidor.accept();
                System.out.println("se acepta conexion");
                //cada que se acepta a un cliente se manda a llamar a un hilo para que atienda su solicitud
                
                dis = new DataInputStream(cliente.getInputStream());
                dos = new DataOutputStream(cliente.getOutputStream());
                Thread t = new Thread(new ClientHandler(dis, dos, cliente,puerto));
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}