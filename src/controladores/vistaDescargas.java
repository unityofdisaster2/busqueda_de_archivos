/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import javafx.event.*;
import javax.swing.JList;
import javax.swing.JOptionPane;
import pnodo.Nodo;

/**
 *
 * @author unityofdisaster
 */
public class vistaDescargas extends javax.swing.JFrame {

    /**
     * Creates new form vistaDescargas
     */
    private Nodo nodo;
    private LinkedHashMap<String,LinkedList<String>> mapaServidores;
    private String[] arreglo_llaves;
    private String nombreArchivo;
    public vistaDescargas(Nodo nodo, LinkedHashMap<String,LinkedList<String>> mapaServidores, String nombreArchivo) {
        this.nodo = nodo;
        this.mapaServidores = mapaServidores;
        this.nombreArchivo = nombreArchivo;
        initComponents();
        arreglo_llaves = new String[mapaServidores.keySet().size()];
        
        arreglo_llaves = mapaServidores.keySet().toArray(arreglo_llaves);
        
        listaClaves.setModel(new javax.swing.AbstractListModel<String>() {
            public int getSize() { return arreglo_llaves.length; }
            public String getElementAt(int i) { return arreglo_llaves[i]; }
        });
        jScrollPane1.setViewportView(listaClaves);
        
        
        
        
        listaClaves.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    System.out.println(listaClaves.getSelectedValue());
                    System.out.println(mapaServidores.get(listaClaves.getSelectedValue()));
                    LinkedList<String> listaHosts = mapaServidores.get(listaClaves.getSelectedValue());
                    new vistaHosts(listaHosts).setVisible(true);
                }
            }
        });        
        this.setTitle("Disponibilidad de archivo");
        progresoDescarga.setMaximum(100);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        listaClaves = new javax.swing.JList<>();
        downloadBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        progresoDescarga = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        listaClaves.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(listaClaves);

        downloadBtn.setText("Descargar");
        downloadBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadBtnActionPerformed(evt);
            }
        });

        jLabel1.setText("Claves MD5");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(181, 181, 181))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(63, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(progresoDescarga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(47, 47, 47)
                        .addComponent(downloadBtn))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 381, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(downloadBtn)
                .addGap(30, 30, 30))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addComponent(progresoDescarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void downloadBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadBtnActionPerformed
        String llaveHash = listaClaves.getSelectedValue();
        LinkedList<String> listaDescargas = mapaServidores.get(llaveHash);
        boolean flag = true;
        if(listaDescargas.contains(nodo.getDireccionIP()+":"+Integer.toString(nodo.getGlobalPort()+100))){
            JOptionPane.showMessageDialog(null, "Archivo ya existe localmente con este MD5", "Atencion", JOptionPane.INFORMATION_MESSAGE);
            flag = false;
        }
        if(flag){
            String hostRemoto;
            int puertoRemoto;
            int contador = 0;
            int frags = listaDescargas.size()/100;
            int progreso = 0;
            for(String equiposRemotos: listaDescargas){
                hostRemoto = equiposRemotos.substring(0, equiposRemotos.indexOf(":"));
                puertoRemoto = Integer.parseInt(equiposRemotos.substring(equiposRemotos.indexOf(":") + 1, equiposRemotos.length()));
                if(contador == 0){
                    nodo.iniciarClienteArchivos(hostRemoto, puertoRemoto);
                    nodo.getFc().conectar();
                    nodo.getFc().prepararStream(nombreArchivo);
                }else{
                    nodo.getFc().cambiarServidor(hostRemoto, puertoRemoto, Integer.toString(nodo.getGlobalPort()));
                    nodo.getFc().conectar();
                }
                nodo.getFc().descargaArchivo(nombreArchivo, listaDescargas.size(), contador+1);
                contador++;
                progreso+=frags;
                progresoDescarga.setValue(progreso);
            }
            nodo.getFc().cerrarStream();
            progresoDescarga.setValue(100);           
            JOptionPane.showMessageDialog(null, "Se ha descagado archivo por completo", "Descarga", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_downloadBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton downloadBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> listaClaves;
    private javax.swing.JProgressBar progresoDescarga;
    // End of variables declaration//GEN-END:variables
}
