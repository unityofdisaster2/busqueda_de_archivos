/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientes;

import controladores.FXMLVentanaPrincipalController;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 *
 * @author sandu
 */
public class MulticastClient implements Runnable {

    private MulticastSocket cl;
    private InetAddress gpo;
    private DatagramPacket recibidos;
    private DatagramPacket ptoEnviado;
    private LinkedList<String> listaPuertos;
    private FXMLVentanaPrincipalController controlVentana;
    private LinkedHashMap<Integer, String> listaServidores;
    private boolean flag = false;
    //se pasa el controlador de los eventos de la ventana 
    public MulticastClient() {

    }

    //se guarda controlador de ventana para actualizar los cambios en la lista de usuarios
    public void setControlador(FXMLVentanaPrincipalController controlVentana) {
        this.controlVentana = controlVentana;
    }

    byte[] b;

    public boolean conectar() {
        try {
            // 9999 el puerto que escucharan todos los clientes
            cl = new MulticastSocket(9999);
            cl.setReuseAddress(true);
            cl.setTimeToLive(1);
            gpo = InetAddress.getByName("228.1.1.1");
            cl.joinGroup(gpo);
            System.out.println("Cliente multicast conectado...");
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean desconectar() {
        try {
            cl.leaveGroup(gpo);
            cl.disconnect();
            cl.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Metodo para enviar puerto y host al servidor y este sea agregado a la
     * lista de nodos activos
     *
     * @param puertoServicio puerto ligado al nodo iniciado
     * @param host direccion ip del nodo iniciado
     */
    public void enviarPuerto(String puertoServicio, String host) {
        b = puertoServicio.getBytes();
        try {

            //se envia la peticion para que se agregue un nuevo nodo
            String peticion = "1";
            ptoEnviado = new DatagramPacket(peticion.getBytes(), peticion.length(), gpo, 9876);
            cl.send(ptoEnviado);
            //Se envia puerto
            ptoEnviado = new DatagramPacket(b, b.length, gpo, 9876);
            cl.send(ptoEnviado);
            //se envia direccion ip
            byte[] ip = host.getBytes();
            ptoEnviado = new DatagramPacket(ip, ip.length, gpo, 9876);
            cl.send(ptoEnviado);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Metodo encargado de mandar la peticion para eliminar el puerto de este
     * nodo al servidor multicast. (Este metodo debe ser invocado cuando se
     * cierre la ventana de la interfaz grafica)
     *
     * @param puertoServicio
     */
    public void eliminarPuerto(String puertoServicio) {
        b = puertoServicio.getBytes();
        try {
            String peticion = "3";
            //se envia peticion para remover puerto
            ptoEnviado = new DatagramPacket(peticion.getBytes(), peticion.length(), gpo, 9876);
            cl.send(ptoEnviado);
            //se envia puerto que se desea eliminar
            ptoEnviado = new DatagramPacket(b, b.length, gpo, 9876);
            cl.send(ptoEnviado);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void actualizarPuertos() {

        //se crean listas que contendran puertos y hosts
        LinkedList<Integer> ptos = new LinkedList<>();
        LinkedList<String> hosts = new LinkedList<>();
        //se agregan todos los puertos a la lista
        ptos.addAll(listaServidores.keySet());
        //se agregan valores ligados a los puertos a la lista de hosts
        for (Integer numbs : ptos) {
            hosts.add(listaServidores.get(numbs));
        }

        int ptSiguiente, ptAnterior, iS, iA;
        String hstSiguiente, hstAnterior;

        //se verifica si hay mas de un nodo activo
        if (ptos.size() > 1) {
            //se consulta el indice del puerto ligado a este nodo
            int index = ptos.indexOf(controlVentana.getNodo().getGlobalPort());

            //condicion para manejar los indices cuando el puerto de este nodo
            //se encuentre en la ultima posicion de la lista
            if (index == ptos.size() - 1) {
                ptSiguiente = ptos.get(0);
                hstSiguiente = hosts.get(0);
                iS = 0;
                iA = index - 1;
                ptAnterior = ptos.get(index - 1);
                hstAnterior = hosts.get(index - 1);

            } //condicion para manejar los indices cuando el puerto se encuentra
            //al principio de la lista
            else if (index == 0) {
                ptSiguiente = ptos.get(index + 1);
                hstSiguiente = hosts.get(index + 1);

                iS = index + 1;
                iA = ptos.size() - 1;

                ptAnterior = ptos.get(ptos.size() - 1);
                hstAnterior = hosts.get(ptos.size() - 1);

            } //funcion para manejar los indices cuando el puerto se encuentre
            //en un punto intermedio
            else {
                ptSiguiente = ptos.get(index + 1);
                hstSiguiente = hosts.get(index + 1);

                iS = index + 1;
                iA = index - 1;

                ptAnterior = ptos.get(index - 1);
                hstAnterior = hosts.get(index - 1);

            }
            
            
            //se establece puerto y host siguiente en el nodo
            controlVentana.getNodo().setPuertoSiguiente(ptSiguiente);
            controlVentana.getNodo().setDireccionSiguiente(hstSiguiente);
            
            //se marca en la lista de la interfaz grafica el nodo siguiente
            String aux = "siguiente:" + listaPuertos.get(iS);
            listaPuertos.set(iS, aux);
            
            //se establece el puerot y host anteriores en el nodo
            controlVentana.getNodo().setPuertoAnterior(ptAnterior);
            controlVentana.getNodo().setDireccionAnterior(hstAnterior);
            
            //se marca puerto anterior en la lista
            aux = "anterior:" + listaPuertos.get(iA);
            listaPuertos.set(iA, aux);
            controlVentana.getNodo().actualizarPuertos();

        }

    }
    
    /**
     * Metodo para actualizar lista de nodos activos cada que el servidor 
     * indique el mensaje
     */
    public void actualizarLista() {
        listaPuertos = new LinkedList<>();
        
        //se guardan valores en lista ligada para presentarlos en un formato
        //de la forma: localhost:numero_puerto
        for (Integer p : listaServidores.keySet()) {
            listaPuertos.add(listaServidores.get(p) + ":" + Integer.toString(p));
            System.out.println(listaServidores.get(p) + ":" + Integer.toString(p));
        }
        
        actualizarPuertos();
        //se actualiza lista en el controlador de la itnerfaz grafica
        controlVentana.actualizarLista(listaPuertos);
    }
    public boolean getFlag(){
        return this.flag;
    }
    
    
    
    public String recibirMensaje() {
        recibidos = new DatagramPacket(new byte[5000], 5000);
        try {
            cl.receive(recibidos);
            String aux = new String(recibidos.getData(), 0, recibidos.getLength());
            //si el nodo que se agrego no es repetido se actualiza lista
            if (aux.equals("actualizar")) {
                //se recibe estructura del servidor y se guarda en linkedhashMap
                cl.receive(recibidos);
                ByteArrayInputStream bais = new ByteArrayInputStream(recibidos.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                listaServidores = (LinkedHashMap<Integer, String>) ois.readObject();

            }
            return aux;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * hilo encargado de recibir peticiones del servidor
     */
    @Override
    public void run() {
        for (;;) {
            String respuesta = recibirMensaje();
            System.out.println(respuesta);
            if (respuesta.equals("actualizar")) {
                actualizarLista();
            }else if(respuesta.equals("-1")){
                flag = true;
            }
        }
    }

}
