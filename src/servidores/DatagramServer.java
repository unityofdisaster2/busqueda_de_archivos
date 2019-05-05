/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidores;

import clientes.DatagramClient;
import controladores.FXMLVentanaPrincipalController;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sandu
 */
public class DatagramServer implements Runnable {

    DatagramSocket s;
    private int ptoSiguiente;
    private int ptoAnterior;
    private int ptoLocal;
    private String hostLocal;
    private InetAddress hostSiguiente;
    private InetAddress hostAnterior;
    private DatagramPacket envios;
    private DatagramPacket recepciones;
    private FXMLVentanaPrincipalController control;
    private DatagramClient dc;
    boolean flag;

    public DatagramServer(int ptoLocal) {
        flag = false;
        this.ptoLocal = ptoLocal;
    }

    /**
     * @return the control
     */
    public FXMLVentanaPrincipalController getControl() {
        return control;
    }

    public void setDc(DatagramClient dc) {
        this.dc = dc;
    }

    /**
     * @param control the control to set
     */
    public void setControl(FXMLVentanaPrincipalController control) {
        this.control = control;
    }

    public void setHostLocal(String hostLocal) {
        this.hostLocal = hostLocal;
    }

    /**
     * @return the ptoSiguiente
     */
    public int getPtoSiguiente() {
        return ptoSiguiente;
    }

    /**
     * @param ptoSiguiente the ptoSiguiente to set
     */
    public void setPtoSiguiente(int ptoSiguiente) {
        this.ptoSiguiente = ptoSiguiente;
    }

    /**
     * @return the ptoAnterior
     */
    public int getPtoAnterior() {
        return ptoAnterior;
    }

    /**
     * @param ptoAnterior the ptoAnterior to set
     */
    public void setPtoAnterior(int ptoAnterior) {
        this.ptoAnterior = ptoAnterior;
    }

    /**
     * @return the ptoLocal
     */
    public int getPtoLocal() {
        return ptoLocal;
    }

    /**
     * @param ptoLocal the ptoLocal to set
     */
    public void setPtoLocal(int ptoLocal) {
        this.ptoLocal = ptoLocal;
    }

    /**
     * @return the hostSiguiente
     */
    public InetAddress getHostSiguiente() {
        return hostSiguiente;
    }

    /**
     * @param hostSiguiente the hostSiguiente to set
     */
    public void setHostSiguiente(String hostSiguiente) {
        try {
            this.hostSiguiente = InetAddress.getByName(hostSiguiente);
        } catch (UnknownHostException ex) {
            Logger.getLogger(DatagramServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void switchFlag() {
        flag = true;
    }

    /**
     * @return the hostAnterior
     */
    public InetAddress getHostAnterior() {
        return hostAnterior;
    }

    /**
     * @param hostAnterior the hostAnterior to set
     */
    public void setHostAnterior(String hostAnterior) {
        try {
            this.hostAnterior = InetAddress.getByName(hostAnterior);
        } catch (UnknownHostException ex) {
            Logger.getLogger(DatagramServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            s = new DatagramSocket(ptoLocal, InetAddress.getByName(hostLocal));

            System.out.println("puerto cliente datagrama:"+s.getPort());
            System.out.println("direccion ip:"+s.getInetAddress());
            String mensaje, archivo, hostConsulta;
            for (;;) {
                System.out.println("servidor entra a ciclo");
                //Enviar primero host y opcionalmente puerto para que el servidor pueda responder
                System.out.println("Sobrepasa la bandera");
                recepciones = new DatagramPacket(new byte[100], 100);
                s.receive(recepciones);

                hostConsulta = new String(recepciones.getData(), 0, recepciones.getLength());

                s.receive(recepciones);

                archivo = new String(recepciones.getData(), 0, recepciones.getLength());    
                
                if (!flag) {
                    System.out.println(recepciones.getPort()+":"+recepciones.getAddress().getHostAddress());
                    control.addMensaje("se recibe peticion de busqueda de archivo: " + archivo);
                    File f = new File(Integer.toString(ptoLocal) + "/" + archivo);
                    if (f.exists()) {
                        control.addMensaje("archivo "+archivo+ " encontrado, se notifica a cliente que solicita");
                        String confirmacion = hostLocal+":"+Integer.toString(ptoLocal + 100);
                        envios = new DatagramPacket(confirmacion.getBytes(), confirmacion.length(), InetAddress.getByName(hostConsulta), recepciones.getPort());
                        s.send(envios);
                    } else {
                        control.addMensaje("archivo "+ archivo+" no encontrado en este nodo, se pregunta al siguiente");
                        mensaje = dc.preguntarArchivo(archivo);
                        envios = new DatagramPacket(mensaje.getBytes(), mensaje.length(), InetAddress.getByName(hostConsulta), recepciones.getPort());
                        //se envia de vuelta
                        s.send(envios);
                    }

                }else{
                    System.out.println("se llega a nodoo de origen");
                    System.out.println(recepciones.getPort()+":"+recepciones.getAddress().getHostAddress());
                    mensaje = "-1";
                    envios = new DatagramPacket(mensaje.getBytes(), mensaje.length(), InetAddress.getByName(hostConsulta), recepciones.getPort());
                    s.send(envios);
                    flag = false;
                    
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
