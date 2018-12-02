package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import server.integration.CatalogDBException;
import server.model.File;

/**
 *
 * @author yuchen
 */
public interface Server extends Remote{
    public static final String SERVER_NAME_IN_REGISTRY = "Server";
    
    long login(Client remoteNode, Credentials credentials) throws RemoteException;
    
    void logout(long id) throws RemoteException;
    
    List<File> list() throws RemoteException, CatalogDBException;
    
    FileDTO open(String filename, long id) throws RemoteException, CatalogDBException;
    
    void upload(long id, String filename, int size, String permission) throws RemoteException;
    
    void delete(String filename, long id) throws RemoteException, CatalogDBException;
    
    void update(String filename, int newSize, long id) throws RemoteException, CatalogDBException;
    
    boolean register(Credentials credentials) throws RemoteException, CatalogDBException;

}
