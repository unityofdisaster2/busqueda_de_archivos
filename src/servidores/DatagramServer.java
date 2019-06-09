/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidores;

import clientes.DatagramClient;
import controladores.FXMLVentanaPrincipalController;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

    
    public void agregarReferenciaLocal(LinkedHashMap<String, LinkedList<String>> mapaExterno, String id_nodo, String md5) {
        LinkedList<String> lAux = new LinkedList<>();
        //si el mapa aun no tiene referencia a archivos se crea su primer valor
        if (mapaExterno.size() == 0) {
            lAux.add(id_nodo);
            mapaExterno.put(md5, lAux);
        }
        //se verifica si alguno de los archivos encontrados tiene el mismo MD5
        //de ser asi se agrega id del nodo a la lista de los que coinciden
        else if (mapaExterno.containsKey(md5)) {
            mapaExterno.get(md5).add(id_nodo);
        }
        //de lo contrario se crea nueva referencia
        else {
            lAux.add(id_nodo);
            mapaExterno.put(md5, lAux);
        }
    }    
    
    
    
    @Override
    public void run() {
        try {
            //se establece conexion de socket de datagramas
            s = new DatagramSocket(ptoLocal, InetAddress.getByName(hostLocal));

            String archivo, hostConsulta;
            for (;;) {
                System.out.println("estado de la bandera:" + flag);

                //se recibe el host del cliente que consulta
                recepciones = new DatagramPacket(new byte[100], 100);
                s.receive(recepciones);

                hostConsulta = new String(recepciones.getData(), 0, recepciones.getLength());

                s.receive(recepciones);

                //se recibe el nombre del archivo consultado
                archivo = new String(recepciones.getData(), 0, recepciones.getLength());
                
                System.out.println("llegan los primeros dos mensajes");
                
                recepciones= new DatagramPacket(new byte[1024*4], 1024*4);
                
                s.receive(recepciones);
                System.out.println("llega mapa");
                //se recibe HashMap de cliente que solicita informacion
                
                ByteArrayInputStream bais = new ByteArrayInputStream(recepciones.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                //se hace lectura de objeto y se castea al tipo de lista requerido
                LinkedHashMap<String,LinkedList<String>> mapaEx = (LinkedHashMap<String,LinkedList<String>>)ois.readObject();                
                
                
                File f = new File(Integer.toString(ptoLocal) + "/" + archivo);
                
                
                //si la bandera no se levanto quiere decir que este nodo es uno distinto al que pregunto inicialmente
                //y se puede realizar la busqueda
                if (flag == false) {
                    System.out.println(recepciones.getPort() + ":" + recepciones.getAddress().getHostAddress());
                    //se despliega en cuadro de mensajes que la consulta paso por este nodo
                    //control.addMensaje("se recibe peticion de busqueda de archivo: " + archivo);
                    
                    if (f.exists()) {
                        //si se encuentra el archivo se despliega mensaje en ventana
                        //control.addMensaje("archivo "+archivo+ " encontrado, se notifica a cliente que solicita");
                        //String confirmacion = hostLocal + ":" + Integer.toString(ptoLocal + 100);
                        MD5CheckSum md5 = new MD5CheckSum();
                        String llaveMD5 = md5.getMD5Checksum(Integer.toString(ptoLocal) + "/" + archivo);
                        agregarReferenciaLocal(mapaEx, hostLocal + ":" + Integer.toString(ptoLocal + 100), llaveMD5);

                    } else {
                        //si el archivo no existe se despliega mensaje notificandolo
                        System.out.println("archivo no encontrado en este nodo");
                        //control.addMensaje("archivo "+ archivo+" no encontrado en este nodo");
                        //se utiliza cliente ligado para preguntar al siguiente nodo si cuenta con el archivo
                    }
                        mapaEx = dc.preguntarArchivo(archivo,mapaEx);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        oos.writeObject(mapaEx);
                        byte [] lst = baos.toByteArray();                        
                 
                        //se envia de vuelta al nodo anterior la respuesta que se haya obtenido del nodo siguiente
                        envios = new DatagramPacket(lst, lst.length, InetAddress.getByName(hostConsulta), recepciones.getPort());
                        //se envia de vuelta
                        s.send(envios);

                } else {
                    //si se llega a esta condicion quiere decir que se le esta pregunta al mismo nodo que hizo la pregunta del archivo
                    //por lo tanto este nodo unicamente regresara el mensaje de error -1
                    System.out.println("se llega a nodo de origen");
                    
                    if(f.exists()){
                        MD5CheckSum md5 = new MD5CheckSum();
                        String llaveMD5 = md5.getMD5Checksum(Integer.toString(ptoLocal) + "/" + archivo);
                        agregarReferenciaLocal(mapaEx, hostLocal + ":" + Integer.toString(ptoLocal + 100), llaveMD5);                        
                    }
                    
                    //se convierte mapa a bytes
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(mapaEx);
                    byte [] lst = baos.toByteArray();                     
                                        
                    System.out.println(recepciones.getPort() + ":" + recepciones.getAddress().getHostAddress());
                    
                    //se despliega en el nodo origen que no se encontro archivo en ningun nodo
                    //control.addMensaje("no se encontro archivo en ningun nodo");
                    envios = new DatagramPacket(lst, lst.length, InetAddress.getByName(hostConsulta), recepciones.getPort());
                    s.send(envios);
                    flag = false;

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /*
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
    }*/

}
