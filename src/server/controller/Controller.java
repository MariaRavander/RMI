package server.controller;

import common.Client;
import common.Credentials;
import common.FileDTO;
import common.Server;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.integration.CatalogDBException;
import server.model.UserManager;
import server.model.Catalog;
import server.model.File;
import java.util.List;

/**
 *
 * @author yuchen
 */
public class Controller  extends UnicastRemoteObject implements Server {
    private final String dbms = "derby";
    private final String datasource = "CatalogDB";
    private final Random idGenerator = new Random();
    private final UserManager userMgr;
    private final Catalog cat;


    public Controller() throws RemoteException, CatalogDBException {
        this.cat = new Catalog(dbms, datasource);
        this.userMgr = new UserManager(dbms, datasource);
    }
    
    @Override
    public boolean register(Credentials credentials) throws RemoteException, CatalogDBException{
        return  userMgr.register(credentials);
    }
    
    @Override
    public long login(Client remoteNode, Credentials credentials) throws RemoteException {
        long id = 0;
        try {
            id = userMgr.createUser(remoteNode, credentials);
        } catch (CatalogDBException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }
    
    @Override
    public void logout(long id) throws RemoteException {
        userMgr.removeUser(id);
    }
    
    @Override
    public List<File> list() throws RemoteException, CatalogDBException {
        return cat.getFiles();
    }
    
    @Override
    public void upload(long id, String filename, int size, String permission) throws RemoteException{
        if(id != 0){
            FileDTO file = cat.makeFile(filename, size, userMgr.getUser(id).username, permission);
            try {
                cat.addFile((File)file);
            } catch (CatalogDBException ex) {
                System.err.println("File could not be uploaded.");
            }
        }
    }
    
    @Override
    public FileDTO open(String file, long id) throws RemoteException, CatalogDBException {
        if(id != 0){
            FileDTO fileDTO = cat.getFile(file);
            String owner = fileDTO.getFileOwner();
            if(userMgr.userName.containsKey(owner) && !owner.equals(userMgr.getUser(id).username)){
                long ido = userMgr.getId(owner);
                String openBy = userMgr.getUser(id).username;
                userMgr.getUser(ido).send("OPEN##" + openBy);
            }
            return fileDTO;
        }
        return null;
    }
    
    @Override
    public void delete(String filename, long id) throws RemoteException, CatalogDBException {
        if(id != 0){
            FileDTO fileDTO = cat.getFile(filename);
            String owner = fileDTO.getFileOwner();
            boolean deleted = cat.deleteFile(filename, userMgr.getUser(id).username);
            if(deleted && !owner.equals(userMgr.getUser(id).username) && userMgr.userName.containsKey(owner)){
                System.out.println("i notify");
                long ido = userMgr.getId(owner);
                String deletedBy = userMgr.getUser(id).username;
                userMgr.getUser(ido).send("DELETE##" + deletedBy);
            }
        }
    }
    
    @Override
    public void update(String filename, int newSize, long id) throws RemoteException, CatalogDBException {
        if(id != 0){
            FileDTO fileDTO = cat.getFile(filename);
            String owner = fileDTO.getFileOwner();
            if(fileDTO.getFilePermission().equals("RW") || owner.equals(userMgr.getUser(id).username))
                cat.updateFile(filename, newSize);
            if(userMgr.userName.containsKey(owner)  && !owner.equals(userMgr.getUser(id).username)){
                long ido = userMgr.getId(owner);
                String updatedBy = userMgr.getUser(id).username;
                userMgr.getUser(ido).send("UPDATE##" + updatedBy);
            }
        }
    }
}

