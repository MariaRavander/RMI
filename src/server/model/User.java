package server.model;

import common.Client;
import common.MessageException;

import java.rmi.RemoteException;

/**
 *
 * @author yuchen
 */
public class User {
    public String username;
    public long id;
    private Client remoteNode;
    private UserManager userMgr;
    private static final String DEFAULT_USERNAME = "anonymous";
    
    public User(String username, Client remoteNode, UserManager mgr, long id){
        this.username = username;
        this.remoteNode = remoteNode;
        this.userMgr = mgr;
        this.id = id;
    }
    
    public User(long id, Client remoteNode, UserManager mgr) {
        this(DEFAULT_USERNAME, remoteNode, mgr, id);
    }
    
    public void send(String msg){
        try {
            remoteNode.recvMsg(msg);
        } catch(RemoteException re) {
            throw new MessageException("Failed to deliver message to " + username + ".");
        }
    }
    
    public boolean hasRemoteNode(Client remoteNode) {
        return remoteNode.equals(this.remoteNode);
    }
    
    
}
