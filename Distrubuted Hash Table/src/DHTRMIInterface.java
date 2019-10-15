import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/*
 * This is the interface for the DHTServerNode class
 * 
 * DHT Servers are connected in a ring. For example, if there are 4 servers each with IDs 0 to 3
 * then they are connected as follows (0 -> 1 -> 2 -> 3 -> 0)
 */
public interface DHTRMIInterface extends Remote, Serializable {

	// Tells the SUCCESSOR DHT Node who they are by indicating their serverID and
	// serverIP
	public void onConnect(int serverID, String serverIP) throws RemoteException;

	// Notifies the SUCCESSOR DHT Node that they are disconnecting
	public void onDisconnect(String name) throws RemoteException;


	// Hashing function for the filename (sum up ascii values and mod by 4)
	public int hashingFunction(String filename) throws RemoteException;

	// Since each P2P client knows the first DHT Server Node, they send their
	// filename through the chain until they reach the DHT server which will hold
	// their hashed filename
	public void sendToNextNode(String filename, String peerIP) throws RemoteException;

	// Propogates a message through the DHT server chain telling the servers to
	// remove each file that client has uploaded since it will be no longer
	// available
	public void removeFileFromDHT(String filename) throws RemoteException;

	// Sends a message through the DHT chain asking who owns a given filename
	public String whoOwnsRecord(String filename) throws RemoteException;

	// Contacts the DHT Servers asking what are all the available files to download
	public Set<String> getAvailableFiles() throws RemoteException;

}