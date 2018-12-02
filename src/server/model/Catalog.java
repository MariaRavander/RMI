package server.model;

import common.Credentials;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import server.integration.CatalogDAO;
import server.integration.CatalogDBException;
/**
 *
 * @author yuchen
 */
public class Catalog {
    private List<File> files = Collections.synchronizedList(new ArrayList<>());
    private final CatalogDAO catDAO;
    
    public Catalog(String dbms, String datasource) throws CatalogDBException {
        this.catDAO = new CatalogDAO(dbms, datasource);
    }
    
    public void addFile(File file) throws CatalogDBException{ 
        catDAO.createFile(file);
        //files.add(file);
    }
    
    public List<File> getFiles() throws CatalogDBException{

        return catDAO.findAllFiles();
    }
    
    public File makeFile(String filename, int size, String owner, String permission){
        File file = new File(filename, size, owner, permission);
        return file;
    }
    
    public File getFile(String filename) throws CatalogDBException{
        return catDAO.findFileByName(filename);
    }
    
    public boolean deleteFile(String filename, String username) throws CatalogDBException {
        boolean deleted = false;
        File fileToDelete = catDAO.findFileByName(filename);
        if(fileToDelete.getFileOwner().equals(username) || fileToDelete.getFilePermission().equals("RW")){
            catDAO.deleteFile(fileToDelete);
            deleted = true;
        }
        return deleted;
    }
    
    public void updateFile(String filename, int newSize) throws CatalogDBException{
        catDAO.updateFile(filename, newSize);
    }
    
    public boolean authentication(Credentials credentials)throws CatalogDBException{
        if(catDAO.getPassword(credentials.getUsername()).equals(credentials.getPassword()))
            return true;
        else
            return false;
    }
}
