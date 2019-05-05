/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidores;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 *
 * @author sandu
 */
public class MulticastServer implements Runnable {

    private MulticastSocket s;
    private DatagramPacket recibidos, enviados;
    private String ptoServicio;
    private byte[] b;
    private InetAddress gpo;
    private LinkedHashMap<Integer, String> puertoServicio;
    String peticion;
    int ptoRecibido;
    String hostRecibido;

    public MulticastServer() {
        gpo = null;
        puertoServicio = new LinkedHashMap<>();
    }

    @Override
    public void run() {
        try {
            this.s = new MulticastSocket(9876);
            System.out.println("servidor multicast iniciado");
            s.setReuseAddress(true);
            s.setTimeToLive(1);
            gpo = InetAddress.getByName("228.1.1.1");
            s.joinGroup(gpo);
            for (;;) {
                //primero se recibe peticion
                recibidos = new DatagramPacket(new byte[100], 100);
                s.receive(recibidos);
                peticion = new String(recibidos.getData(), 0, recibidos.getLength());

                if (peticion.equals("1")) {
                    s.receive(recibidos);
                    //primero se recibe el puerto global de un nodo que se haya levantado
                    ptoRecibido = Integer.parseInt(new String(recibidos.getData(), 0, recibidos.getLength()));
                    s.receive(recibidos);
                    //posteriormente se recibe la direccion IP
                    hostRecibido = new String(recibidos.getData(), 0, recibidos.getLength());
                    if (!puertoServicio.containsKey(ptoRecibido)) {
                        puertoServicio.put(ptoRecibido, hostRecibido);

                        //se manda mensaje para que el cliente se prepare para actualizar la lista de nodos activos
                        //nota: este paso se podria quitar ya que se diseño el servidor para que en los unicos mensajes
                        //que maneja se actualice la tabla
                        String aux = "actualizar";
                        enviados = new DatagramPacket(aux.getBytes(), aux.length(), gpo, 9999);
                        s.send(enviados);

                        int[] auxArr = new int[puertoServicio.size()];
                        int index = 0;
                        //se guardan los nodos por orden de aparicion en un arreglo auxiliar
                        for (int llaves : puertoServicio.keySet()) {
                            auxArr[index] = llaves;
                            index++;
                        }
                        //se ordenan puertos en forma ascendente
                        Arrays.sort(auxArr);

                        LinkedHashMap<Integer, String> res = new LinkedHashMap<>();
                        //se guardan nodos y su host asociados en una nueva estructura
                        for (int i = 0; i < auxArr.length; i++) {
                            res.put(auxArr[i], puertoServicio.get(auxArr[i]));
                        }

                        //se convierte estructura en arreglo de bytes para ser enviado como datagrama
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        oos.writeObject(res);
                        byte[] lst = baos.toByteArray();
                        // se envia estructura al grupo que este conectado al puerto 9999
                        enviados = new DatagramPacket(lst, lst.length, gpo, 9999);
                        s.send(enviados);

                    } else {
                        String error = "-1";
                        enviados = new DatagramPacket(error.getBytes(), error.length(), gpo, 9999);
                        s.send(enviados);
                    }

                }

                if (peticion.equals("3")) {
                    s.receive(recibidos);

                    String pReceived = new String(recibidos.getData(), 0, recibidos.getLength());
                    System.out.println("Tamano de HashMap: " + puertoServicio.size());
                    //se retira puerto recibido de la lista y se manda orden para actualizar la lista e interfaz en los clientes
                    puertoServicio.remove(Integer.parseInt(pReceived));

                    System.out.println("Tamano de HashMap: " + puertoServicio.size());
                    String aux = "actualizar";
                    enviados = new DatagramPacket(aux.getBytes(), aux.length(), gpo, 9999);
                    s.send(enviados);
                    //se repite el proceso de ordenamiento (se podria hacer en una sola funcion para no repetir codigo
                    int[] auxArr = new int[puertoServicio.size()];
                    int index = 0;
                    for (int llaves : puertoServicio.keySet()) {
                        auxArr[index] = llaves;
                        index++;
                    }

                    Arrays.sort(auxArr);

                    LinkedHashMap<Integer, String> res = new LinkedHashMap<>();
                    for (int i = 0; i < auxArr.length; i++) {
                        res.put(auxArr[i], puertoServicio.get(auxArr[i]));
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(res);
                    byte[] lst = baos.toByteArray();
                    enviados = new DatagramPacket(lst, lst.length, gpo, 9999);
                    s.send(enviados);

                }

                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MulticastServer ms = new MulticastServer();
        Thread t1 = new Thread(ms);
        t1.start();
    }

}
