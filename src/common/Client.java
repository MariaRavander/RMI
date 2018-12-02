package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author yuchen
 */
public interface Client extends Remote{
    void recvMsg(String msg) throws RemoteException;
}
