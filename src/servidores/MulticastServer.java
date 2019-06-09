/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidores;

import java.net.*;
//import javax.swing.JOptionPane;

/**
 *
 * @author sandu
 */
public class MulticastServer implements Runnable {
    
    
    /*
    //probar servidor con hilo
    public static void main(String[] args) {
        
        String pto = JOptionPane.showInputDialog(null, "ingrese un numero de puerto", "numero puerto");
        MulticastServer ms = new MulticastServer(Integer.parseInt(pto));
        Thread t1 = new Thread(ms);
        t1.start();
    }*/    
    
    private MulticastSocket s;
    private int puertoServicio;
    
    
   
    public void setPuertoServicio(int puertoServicio){
        this.puertoServicio = puertoServicio;
    }
    
    /**
     * Constructor necesita el puerto de servicio del servidor de datagramas local
     * @param puertoServicio 
     */
    public MulticastServer(int puertoServicio){
        this.puertoServicio = puertoServicio;
    }

    @Override
    public void run() {
        InetAddress gpo = null;
        try {
            //conectar 
            MulticastSocket s = new MulticastSocket(9876);
            s.setReuseAddress(true);
            s.setTimeToLive(1);
            //se enviara como mensaje el puerto de datagramas en forma de cadena
            String msj = Integer.toString(puertoServicio);
            byte[] b = msj.getBytes();
            gpo = InetAddress.getByName("228.1.1.1");
            s.joinGroup(gpo);
            for(;;){
                
                DatagramPacket p = new DatagramPacket(b,b.length, gpo,9999);
                s.send(p);
                try {
                    Thread.sleep(5000);//espera 5 segundos para volver a enviar el paquete
                } catch (InterruptedException io) {
                    io.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

}
