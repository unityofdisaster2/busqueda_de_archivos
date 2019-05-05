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
public class MulticastClient implements Runnable{
    MulticastSocket cl;
    InetAddress gpo;
    DatagramPacket recibidos;
    DatagramPacket ptoEnviado;
    LinkedList<String> listaPuertos;
    FXMLVentanaPrincipalController controlVentana;
    LinkedHashMap<Integer, String> listaServidores;
    //se pasa el controlador de los eventos de la ventana 
    public MulticastClient(){
        
    }
    
    //se guarda controlador de ventana para actualizar los cambios en la lista de usuarios
    public void setControlador(FXMLVentanaPrincipalController controlVentana){
        this.controlVentana = controlVentana;
    }
    
    byte[] b;
    public boolean conectar(){
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
    
    public boolean desconectar(){
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
    
    
    //procurar unicamente llamar a este metodo cuando se abre la ventana
    public void enviarPuerto(String puertoServicio,String host){
        b = puertoServicio.getBytes();
            try {
                
                //se envia la peticion para que se agregue un nuevo nodo
                String peticion = "1";
                ptoEnviado = new DatagramPacket(peticion.getBytes(),peticion.length(),gpo,9876);
                cl.send(ptoEnviado);
                //Se envia puerto
                ptoEnviado = new DatagramPacket(b,b.length,gpo,9876);
                cl.send(ptoEnviado);
                //se envia direccion ip
                byte[] ip = host.getBytes();
                ptoEnviado = new DatagramPacket(ip,ip.length,gpo,9876);
                cl.send(ptoEnviado);
                
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        
    }
    
    public void eliminarPuerto(String puertoServicio){
        b = puertoServicio.getBytes();
        try{
            String peticion = "3";
            ptoEnviado = new DatagramPacket(peticion.getBytes(), peticion.length(),gpo,9876);
            cl.send(ptoEnviado);
            ptoEnviado = new DatagramPacket(b, b.length,gpo,9876);
            cl.send(ptoEnviado);
            
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    
    public void actualizarPuertos(){
        LinkedList<Integer> ptos = new LinkedList<>();
        LinkedList<String> hosts = new LinkedList<>();
        ptos.addAll(listaServidores.keySet());
        for(Integer numbs: ptos){
            hosts.add(listaServidores.get(numbs));
        }
        
        int ptSiguiente,ptAnterior,iS,iA;
        String hstSiguiente,hstAnterior;
        
        if(ptos.size()> 1){
            int index = ptos.indexOf(controlVentana.getNodo().getGlobalPort());
            
            if(index == ptos.size()-1){
                ptSiguiente = ptos.get(0);
                hstSiguiente = hosts.get(0);
                iS = 0;
                iA = index-1;
                ptAnterior = ptos.get(index-1);
                hstAnterior = hosts.get(index-1);

                


            }else if(index == 0){
                ptSiguiente = ptos.get(index+1);
                hstSiguiente = hosts.get(index+1);
                
                iS = index+1;
                iA = ptos.size()-1;

                
                ptAnterior = ptos.get(ptos.size()-1);
                hstAnterior = hosts.get(ptos.size()-1);

                
            }else{
                ptSiguiente = ptos.get(index+1);
                hstSiguiente = hosts.get(index+1);

                iS = index+1;
                iA = index-1;
                
                ptAnterior = ptos.get(index-1);
                hstAnterior = hosts.get(index-1);

            }
                controlVentana.getNodo().setPuertoSiguiente(ptSiguiente);
                controlVentana.getNodo().setDireccionSiguiente(hstSiguiente);
                

                String aux = "siguiente:"+listaPuertos.get(iS);
                listaPuertos.set(iS, aux);

                
                controlVentana.getNodo().setPuertoAnterior(ptAnterior);
                controlVentana.getNodo().setDireccionAnterior(hstAnterior);
                

                aux = "anterior:"+listaPuertos.get(iA);
                listaPuertos.set(iA, aux);      
                controlVentana.getNodo().actualizarPuertos();

        }

    }
    
    public void actualizarLista(){
        listaPuertos = new LinkedList<>();
        
        
        for(Integer p: listaServidores.keySet()){
            listaPuertos.add(listaServidores.get(p)+":"+Integer.toString(p));
            System.out.println(listaServidores.get(p)+":"+Integer.toString(p));
        }
        
        actualizarPuertos();
        controlVentana.actualizarLista(listaPuertos);
    }
    public String recibirMensaje(){
        recibidos = new DatagramPacket(new byte[5000],5000);
        try{
            cl.receive(recibidos);
            String aux = new String(recibidos.getData(),0,recibidos.getLength());
            if(aux.equals("actualizar")){
                cl.receive(recibidos);
                ByteArrayInputStream bais = new ByteArrayInputStream(recibidos.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                listaServidores = (LinkedHashMap<Integer, String>)ois.readObject();
                
                
                

                
                
                return aux;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }
    

     

    @Override
    public void run() {
        for(;;){
                String respuesta = recibirMensaje();
                System.out.println(respuesta);
                if(respuesta.equals("actualizar")){
                    actualizarLista();
                }
        }
    }

     
}
