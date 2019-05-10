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
    
    //activar bandera que indica si este nodo es el que inicio la consulta del archivo a los demas
    public void turnOnFlag() {
        flag = true;
    }
    
    public void turnOffFlag(){
        flag = false;
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
            //se establece conexion de socket de datagramas
            s = new DatagramSocket(ptoLocal, InetAddress.getByName(hostLocal));

            String mensaje, archivo, hostConsulta;
            for (;;) {
                System.out.println("estado de la bandera:"+flag);
                
                
                //se recibe el host del cliente que consulta
                recepciones = new DatagramPacket(new byte[100], 100);
                s.receive(recepciones);

                hostConsulta = new String(recepciones.getData(), 0, recepciones.getLength());

                s.receive(recepciones);
                
                //se recibe el nombre del archivo consultado
                archivo = new String(recepciones.getData(), 0, recepciones.getLength());    
                
                //si la bandera no se levanto quiere decir que este nodo es uno distinto al que pregunto inicialmente
                //y se puede realizar la busqueda
                if (flag == false) {
                    System.out.println(recepciones.getPort()+":"+recepciones.getAddress().getHostAddress());
                    //se despliega en cuadro de mensajes que la consulta paso por este nodo
                    control.addMensaje("se recibe peticion de busqueda de archivo: " + archivo);
                    File f = new File(Integer.toString(ptoLocal) + "/" + archivo);
                    if (f.exists()) {
                        //si se encuentra el archivo se despliega mensaje en ventana
                        control.addMensaje("archivo "+archivo+ " encontrado, se notifica a cliente que solicita");
                        String confirmacion = hostLocal+":"+Integer.toString(ptoLocal + 100);
                        //se envia de vuelta mensaje de confirmacion que esta compuesto por el host y el puerto de flujo del nodo que lo encontro
                        envios = new DatagramPacket(confirmacion.getBytes(), confirmacion.length(), InetAddress.getByName(hostConsulta), recepciones.getPort());
                        s.send(envios);
                    } else {
                        //si el archivo no existe se despliega mensaje notificandolo
                        control.addMensaje("archivo "+ archivo+" no encontrado en este nodo");
                        //se utiliza cliente ligado para preguntar al siguiente nodo si cuenta con el archivo
                        mensaje = dc.preguntarArchivo(archivo);
                        //se envia de vuelta al nodo anterior la respuesta que se haya obtenido del nodo siguiente
                        envios = new DatagramPacket(mensaje.getBytes(), mensaje.length(), InetAddress.getByName(hostConsulta), recepciones.getPort());
                        //se envia de vuelta
                        s.send(envios);
                    }

                }else{
                    //si se llega a esta condicion quiere decir que se le esta pregunta al mismo nodo que hizo la pregunta del archivo
                    //por lo tanto este nodo unicamente regresara el mensaje de error -1
                    System.out.println("se llega a nodoo de origen");
                    
                    System.out.println(recepciones.getPort()+":"+recepciones.getAddress().getHostAddress());
                    mensaje = "-1";
                    //se despliega en el nodo origen que no se encontro archivo en ningun nodo
                    control.addMensaje("no se encontro archivo en ningun nodo");
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
