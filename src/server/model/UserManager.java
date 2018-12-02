package server.model;

import common.Client;
import common.Credentials;
import server.integration.CatalogDAO;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import server.integration.CatalogDBException;


/**
 *
 * @author yuchen
 */
public class UserManager {
    private final Random idGenerator = new Random();
    private final Map<Long, User> users = Collections.synchronizedMap(new HashMap<>());
    public final Map<String, Long> userName = Collections.synchronizedMap(new HashMap<>());
    private final CatalogDAO catDAO;
    
    public UserManager(String dbms, String datasource) throws CatalogDBException {
        this.catDAO = new CatalogDAO(dbms, datasource);
    }
    
    public boolean register(Credentials credentials) throws CatalogDBException{
        if(!catDAO.userExists(credentials.getUsername())){
            catDAO.register(credentials);
            return true;
        }else
            return false;
    }
    
    public long createUser(Client remoteNode, Credentials credentials) throws CatalogDBException {
        if(catDAO.getPassword(credentials.getUsername()).equals(credentials.getPassword())){
            long uid = idGenerator.nextLong() + 1;
            User newUser = new User(credentials.getUsername(),
                                                     remoteNode, this, uid);
            users.put(uid, newUser);
            userName.put(credentials.getUsername(), uid);
            return uid;
        }
        else
            return 0;
    }
    
    public long getId(String name) {
        return userName.get(name);
    }
    
    
    public User getUser(long id) {
        return users.get(id);
    }
    
    public void removeUser(long id) {
        userName.remove(getUser(id).username);
        users.remove(id);
    } 
}
