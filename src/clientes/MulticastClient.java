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
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 *
 * @author sandu
 */
public class MulticastClient implements Runnable {

    private LinkedList<String> listaPuertos;
    private FXMLVentanaPrincipalController controlVentana;
    private LinkedHashMap<String, Boolean> estadoServidores;
    private LinkedHashMap<String, Integer> listaServidores;
    private LinkedHashMap<Integer, String> ordenPuertos;
    private int cantidad_servidores_actual;
    private int cantidad_servidores_anterior;
    private int contador_prueba;
    
    //se pasa el controlador de los eventos de la ventana 
    public MulticastClient() {
        estadoServidores = new LinkedHashMap<>();
        listaServidores = new LinkedHashMap<>();
        ordenPuertos = new LinkedHashMap<>();
        cantidad_servidores_actual = 0;
        cantidad_servidores_anterior = 0;
        contador_prueba = 0;
    }

    //se guarda controlador de ventana para actualizar los cambios en la lista de usuarios
    public void setControlador(FXMLVentanaPrincipalController controlVentana) {
        this.controlVentana = controlVentana;
    }

    public void actualizarPuertos() {

        //se crean listas que contendran puertos y hosts
        LinkedList<Integer> ptos = new LinkedList<>();
        LinkedList<String> hosts = new LinkedList<>();
        //se agregan todos los puertos a la lista
        ptos.addAll(ordenPuertos.keySet());
        //se agregan valores ligados a los puertos a la lista de hosts
        for (Integer numbs : ptos) {
            hosts.add(ordenPuertos.get(numbs));
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

            controlVentana.actualizarEtiquetas(ptAnterior, ptSiguiente);

            //se establece puerto y host siguiente en el nodo
            controlVentana.getNodo().setPuertoSiguiente(ptSiguiente);
            controlVentana.getNodo().setDireccionSiguiente(hstSiguiente);

            //se marca en la lista de la interfaz grafica el nodo siguiente
            //String aux = "siguiente:" + listaPuertos.get(iS);
            //listaPuertos.set(iS, aux);

            //se establece el puerot y host anteriores en el nodo
            controlVentana.getNodo().setPuertoAnterior(ptAnterior);
            controlVentana.getNodo().setDireccionAnterior(hstAnterior);

            //se marca puerto anterior en la lista
            //aux = "anterior:" + listaPuertos.get(iA);
            //listaPuertos.set(iA, aux);
            controlVentana.getNodo().actualizarPuertos();

        } else {
            controlVentana.actualizarEtiquetas(0, 0);
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
        for (Integer p : ordenPuertos.keySet()) {
            listaPuertos.add(listaServidores.get(p) + ":" + Integer.toString(p));
            System.out.println(listaServidores.get(p) + ":" + Integer.toString(p));
        }

        actualizarPuertos();
        //se actualiza lista en el controlador de la itnerfaz grafica
        controlVentana.actualizarLista(listaPuertos);
    }
    
    
    public void ordenarPuertos(){
        int[] auxArr = new int[ordenPuertos.size()];
        int index = 0;
        //se guardan los nodos por orden de aparicion en un arreglo auxiliar
        for (int llaves : ordenPuertos.keySet()) {
            auxArr[index] = llaves;
            index++;
        }
        //se ordenan puertos en forma ascendente
        Arrays.sort(auxArr);

        LinkedHashMap<Integer, String> res = new LinkedHashMap<>();
        //se guardan nodos y su host asociados en una nueva estructura
        for (int i = 0; i < auxArr.length; i++) {
            res.put(auxArr[i], ordenPuertos.get(auxArr[i]));
        }
        ordenPuertos = res;
    }
    
    public void actualizarListaInterfaz(){
        LinkedList<String> listaInterfaz = new LinkedList<>();
        for(Integer ptos: ordenPuertos.keySet()){
            listaInterfaz.add(ordenPuertos.get(ptos)+":"+ptos+" "+listaServidores.get(ordenPuertos.get(ptos)+":"+ptos));
        }
        actualizarPuertos();
        controlVentana.actualizarLista(listaInterfaz);
        //controller.updateTabla(listaInterfaz);
    }



    

    @Override
    public void run() {
        /*
        Se crea otro hilo que se encargara de ir actualizando la lista de 
        servidores en la interfaz grafica 
         */
        Runnable checarEstados = new Runnable() {
            public void run() {
                boolean flag = true;
                LinkedHashMap<String, Integer> copiaMapa = new LinkedHashMap<>();
                for (;;) {
                    try {
                        //se recorren los ids de los servidores
                        for (String llaves : estadoServidores.keySet()) {
                            //si no ha llegado mensaje de datagrama del servidor
                            if (!estadoServidores.get(llaves)) {
                                //se decrementa en uno el contador del servidor
                                listaServidores.put(llaves, listaServidores.get(llaves) - 1);
                            }
                        }
                        copiaMapa.clear();
                        copiaMapa.putAll(listaServidores);
                        for (String llaves : copiaMapa.keySet()) {
                            //se muestran los elementos de la lista en pantalla
                            //System.out.println(llaves + " " + listaServidores.get(llaves));
                            //si uno de los contadores llega a cero se elimina su id ligado
                            if (copiaMapa.get(llaves) == 0) {
                                listaServidores.remove(llaves);
                                estadoServidores.remove(llaves);
                                ordenPuertos.remove(Integer.parseInt(llaves.substring(llaves.indexOf(":") + 1, llaves.length())));
                                flag = false;
                            }
                        }
                        if (!flag || cantidad_servidores_actual != cantidad_servidores_anterior) {
                            ordenarPuertos();

                            cantidad_servidores_anterior = cantidad_servidores_actual;
                            flag = true;
                        }
                        actualizarListaInterfaz();

                        //controller.updateTabla();
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

            }
        };
        Thread t1 = new Thread(checarEstados);
        t1.start();
        InetAddress gpo = null;
        try {
            MulticastSocket cl = new MulticastSocket(9999);
            System.out.println("Cliente escuchando puerto: " + cl.getLocalPort());
            //para poder usar varios clientes debemos hacer que la direccion sea reutilizable
            cl.setReuseAddress(true);

            try {
                //nombre o numero del grupo
                gpo = InetAddress.getByName("228.1.1.1");
            } catch (UnknownHostException u) {
                System.err.println("Direccion erronea");
            }
            //se une cliente al grupo definido anteriormente
            cl.joinGroup(gpo);
            System.out.println("Unido al grupo");

            byte[] b;
            for (;;) {
                //en el ciclo se estaran recibiendo mensajes del grupo
                DatagramPacket p = new DatagramPacket(new byte[10], 10);
                cl.receive(p);

                String msj = new String(p.getData(), 0, p.getLength());
                //System.out.println("Datagrama recibido: " + msj + " desde:" + p.getAddress().getHostAddress());
                String llave = p.getAddress().getHostAddress() + ":" + msj;
                //cuando se reciba un paquete de algun servidor se inicia o reinicia su contador en 11
                listaServidores.put(llave, 11);
                //se pone su estado en true para saber que aun esta mandando mensajes el servidor
                estadoServidores.put(llave, true);

                //Se actualiza la cantidad de servidores
                cantidad_servidores_actual = estadoServidores.size();

                //se agrega puerto e ip a hashMap
                ordenPuertos.put(Integer.parseInt(msj), p.getAddress().getHostAddress());

                //se actualizan los estados de los servidores
                //Se pone en false aquellos que no hayan mandado mensaje recientemente
                for (String llaves : listaServidores.keySet()) {
                    if (llaves != llave) {
                        estadoServidores.put(llaves, false);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
