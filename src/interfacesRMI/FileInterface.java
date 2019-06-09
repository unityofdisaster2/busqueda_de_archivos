/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfacesRMI;

/**
 *
 * @author unityofdisaster
 */
import java.rmi.*;

public interface FileInterface extends Remote{
    
    public byte[] downloadFile(String fileName) throws RemoteException;
    public byte[] downloadMultiHostFile(String fileName, int n_archivos, int n_iter) throws RemoteException;
}
