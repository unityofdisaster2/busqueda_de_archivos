/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author sandu
 */
public class Tester implements Runnable{
    DatagramSocket s;
    DatagramPacket envios;
    DatagramPacket recepciones;
    int puerto;
    public Tester(){
        puerto = 9000;
    }
    
     @Override
    public void run() {
        try {
            s = new DatagramSocket(puerto);
            System.out.println(s.getPort());
            //System.out.println(s.getInetAddress().getHostAddress());
            for(;;){
                recepciones = new DatagramPacket(new byte[100], 100);
                s.receive(recepciones);
                System.out.println(new String(recepciones.getData(),0,recepciones.getLength())+recepciones.getPort()+":"+recepciones.getAddress().getHostAddress());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }   
    
    
    
    public static void main(String[] args) {
        boolean bandera = false;
        System.out.println(bandera);
        if(!bandera){
            System.out.println("lo que sea");
        }
        System.out.println(bandera);
        

    }


}
