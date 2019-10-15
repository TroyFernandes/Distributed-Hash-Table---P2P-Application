
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * This is an interface for the P2P Client/Server part of the application
 */
public interface P2PRMIInterface extends Remote, Serializable {
	// Opens a connection to another P2P Client
	// This method opens a connection and requests the file the client has
	public void createConnection(String IP, int port, String filename) throws RemoteException;
}
