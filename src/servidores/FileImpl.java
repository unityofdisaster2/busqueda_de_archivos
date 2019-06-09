/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidores;

/**
 *
 * @author unityofdisaster
 */
import java.io.*;
import java.rmi.RemoteException;
import interfacesRMI.FileInterface;

public class FileImpl implements FileInterface {
    private String directorio;
    public FileImpl(String directorio){
        this.directorio=directorio;
    }
    
    @Override
    public byte[] downloadFile(String fileName) throws RemoteException {
        try {
            //agregar una variable que contenga el nombre de la carpeta
            //File file = new File("9000/" + fileName);
            File file = new File(directorio+"/" + fileName);

            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            //se crea un arreglo de bytes del tamano del archivo
            byte buffer[] = new byte[(int) file.length()];
           
            //BufferedInputStream input = new BufferedInputStream(new FileInputStream("9000/" + fileName));
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(directorio+"/" + fileName));
            //se hace lectura de bytes del archivo y se guardan en el buffer
            input.read(buffer, 0, buffer.length);
            input.close();
            //se retorna buffer con los bytes correspondientes al archivo
            return (buffer);
        } catch (Exception e) {
            System.out.println("FileImpl: " + e.getMessage());
            e.printStackTrace();
            return (null);
        }
    }

    @Override
    public byte[] downloadMultiHostFile(String fileName,int n_archivos,int n_iter) throws RemoteException {
        try {
            
            //se abre el archivo solicitado por cliente
            
            //File file = new File("9000/" + fileName);
            File file = new File(directorio+"/" + fileName);
            //se genera un tamano de fragmento que sera tomado de cada servidor
            int tamano = (int)file.length()/n_archivos;

            //se crea un stream de datos para guardar los bytes del archivo
            //BufferedInputStream input = new BufferedInputStream(new FileInputStream("9000/" + fileName));
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(directorio+"/" + fileName));
            System.out.println("inicio: "+(n_iter-1)*tamano+" final:" + n_iter*tamano+" total:"+file.length());
            
            byte buffer[];
            
            //si se llega al ultimo host
            if(n_iter == n_archivos){
                buffer = new byte[(int)file.length()-(n_iter-1)*tamano];
                //se omiten n-1 fragmentos de bytes que ya fueron leidos anteriormente
                for(int i = 1; i < n_iter;i++){
                    input.skip(tamano);
                }
                //se hace lectura del fragmento que nos interesa
                input.read(buffer, 0, buffer.length);
            }
            //primer host
            else if(n_iter == 1){
                buffer = new byte[tamano];
                input.read(buffer, 0, tamano);
                
            }else{
                //si el host esta entre el segundo y el penultimo
                buffer = new byte[tamano];
                //se omiten n-1 fragmentos de bytes que ya fueron leidos anteriormente
                for(int i = 1; i < n_iter;i++){
                    input.skip(tamano);
                }
                //se hace lectura del fragmento que nos interesa
                input.read(buffer, 0, tamano);
            }
            
            input.close();
            //se envia el fragmento de bytes leidos en este host
            return (buffer);
        } catch (Exception e) {
            System.out.println("FileImpl: " + e.getMessage());
            e.printStackTrace();
            return (null);
        }
    }
}
