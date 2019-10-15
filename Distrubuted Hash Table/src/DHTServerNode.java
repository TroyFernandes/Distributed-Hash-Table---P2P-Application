import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DHTServerNode extends UnicastRemoteObject implements DHTRMIInterface {

	/*
	 * ALL @OVERRIDE METHOD DEFINITONS ARE LOCATED IN DHTRMIInterface.java
	 */


	private static DHTRMIInterface DHT_Node;

	// Current DHT Server Information
	private static String currentDHTServerIP;
	private static int currentDHTServerPort;
	private static int DHTServerID;



	// Next DHT Server Information
	private static String nextDHTServerIP;
	private static int nextDHTServerPort;

	// Needed for RMI
	private static final long serialVersionUID = 1L;

	// Each DHT Server is aware of their successor node in the format <ServerID, IP>
	private static HashMap<Integer, String> clientList = new HashMap<Integer, String>();


	// Personal Hash Table in the format <Content Name, IP Address>
	private static HashMap<String, String> hashTable = new HashMap<String, String>();

	// All Available Files
	private static Set<String> allFilenames = new HashSet<String>();


	// In this project, the ID Space is = {0, 1, 2, 3}
	// 0 MOD 4 = 0
	// 1 MOD 4 = 1
	// 2 MOD 4 = 2
	// 3 MOD 4 = 3
	// 4 MOD 4 = 0

	// There are FOUR DHT PEERS! these are not clients! which means
	// If we have 4 DHT Servers, their ID's will be 0, 1, 2, and 3


	// After the peer hashes the filename, they know that they can find the content
	// on server (hashedFilename + 1)



	protected DHTServerNode() throws RemoteException, MalformedURLException, RemoteException, NotBoundException {

		super();

	}

	public static void main(String[] args) {



		try {


			// Command line arguments are as follows:
			// args[0] = Current Server IP
			// args[1] = Current Server Port #
			// args[2] = Next DHT Server IP
			// args[3] = Next DHT Server Port
			// args[4] = Current DHT Server ID #
			if (args[0] == null || args[1] == null || args[2] == null || args[3] == null || args[4] == null) {
				System.out.println("No port #'s specified");
				return;
			}


			currentDHTServerIP = args[0];
			currentDHTServerPort = Integer.parseInt(args[1]);
			DHTServerID = Integer.parseInt(args[4]);

			nextDHTServerIP = args[2];
			nextDHTServerPort = Integer.parseInt(args[3]);



			// Create the RMI Registry on Port # = currentDHTServerPort
			LocateRegistry.createRegistry(currentDHTServerPort);
			Naming.rebind("//" + currentDHTServerIP + ":" + currentDHTServerPort + "/MyServer", new DHTServerNode());
			System.err.println("Server ready");

			// Wait a little bit for the servers to initialize
			TimeUnit.SECONDS.sleep(10);


			// Connect to next DHTServerNode
			DHT_Node = (DHTRMIInterface) Naming.lookup("//" + nextDHTServerIP + ":" + nextDHTServerPort + "/MyServer");
			DHT_Node.onConnect(DHTServerID, currentDHTServerIP + ":" + currentDHTServerPort);

			// Print the current DHT Servers' hash table (should be empty)
			printHashTable(hashTable);

		} catch (Exception e) {

			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();

		}

	}

	@Override
	public void onConnect(int serverID, String serverIP) throws RemoteException {
		// TODO Auto-generated method stub
		clientList.put(serverID, serverIP);

		System.out.println("\nDHT Server #" + serverID + " has connected with IP: " + serverIP);

		System.out.println(Arrays.asList(clientList));

	}

	@Override
	public void onDisconnect(String name) throws RemoteException {
		System.out.println(name + " has disconnected!");
		clientList.remove(name);

	}


	@Override
	public int hashingFunction(String filename) {
		int asciiSum = 0;
		for (int i = 0; i < filename.length(); i++) {
			char c = filename.charAt(i);
			asciiSum += (int) c;
		}
		int result = asciiSum % 4;
		System.out.println(filename + " Hashed into: " + result);
		System.out.println("DHT ServerID holding record: " + (result + 1));
		return result + 1;
	}


	// Print the DHT Servers' hash table with information about the current DHT
	// server node and next DHT server node
	private static void printHashTable(HashMap<String, String> hashTable) {
		System.out.println(new String(new char[50]).replace("\0", "\r\n"));
		System.out.println("------------CURRENT DHT SERVER------------------------------------------");
		System.out.println("Sever IP: " + currentDHTServerIP);
		System.out.println("Server Port: " + currentDHTServerPort);

		System.out.println("------------NEXT DHT SERVER------------------------------------------");
		System.out.println("Next Node IP: " + nextDHTServerIP);
		System.out.println("Next Node Port: " + nextDHTServerPort);



		System.out.println("HASH TABLE");
		System.out.println(Arrays.asList(hashTable));

	}

	@Override
	public void sendToNextNode(String filename, String peerIP) throws RemoteException {
		allFilenames.add(filename);
		int ID = hashingFunction(filename) - 1;
		if (ID == DHTServerID) {
			hashTable.put(filename, peerIP);
			printHashTable(hashTable);
		} else {
			// Send to the next server
			System.out.println("Sending to DHT Server: " + nextDHTServerIP + ":" + nextDHTServerPort);
			DHT_Node.sendToNextNode(filename, peerIP);
		}
	}

	@Override
	public void removeFileFromDHT(String filename) throws RemoteException {
		allFilenames.remove(filename);
		int ID = hashingFunction(filename) - 1;
		if (ID == DHTServerID) {
			hashTable.remove(filename);
			printHashTable(hashTable);
		} else {
			// Send to the next server
			System.out.println("Sending to DHT Server: " + nextDHTServerIP + ":" + nextDHTServerPort);
			DHT_Node.removeFileFromDHT(filename);
		}
	}

	@Override
	public String whoOwnsRecord(String filename) throws RemoteException {

		if (!filename.matches("([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)")) {
			return "400";
		}
		System.out.println("WHO OWNS THIS?");
		printHashTable(hashTable);

		int ID = hashingFunction(filename) - 1;
		if (ID == DHTServerID) {
			System.out.println("I own the record for: " + filename);
			System.out.println(hashTable.get(filename));

			if (hashTable.get(filename) == null) {
				return "404";
			}
			return hashTable.get(filename);


		} else {
			// Send to the next server
			return DHT_Node.whoOwnsRecord(filename);
		}

	}

	@Override
	public Set<String> getAvailableFiles() throws RemoteException {
		return allFilenames;
	}









}