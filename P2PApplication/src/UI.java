
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.MalformedURLException;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UI extends UnicastRemoteObject implements P2PRMIInterface {

	/**
	 * P2P GUI
	 */
	private static final long serialVersionUID = 1L;


	// Each P2P client knows the first DHT server node
	private static DHTRMIInterface look_up;

	private static P2PRMIInterface p2pClient;
	private static int P2PRMIPort;

	// Each P2P client keeps a record of what file they are uploading as well as the
	// location on their local computer
	private static HashMap<String, String> fileLocation = new HashMap<String, String>();

	private static String P2PIP;
	private static int P2PPort;


	private static String firstDHTServerIP;
	private static String firstDHTServerPort;

	protected UI() throws RemoteException {
		super();
	}



	public static void main(String args[]) throws MalformedURLException, RemoteException, NotBoundException {

		if (args[0] == null || args[1] == null || args[2] == null || args[3] == null) {
			System.out.println("No IP and/or Port # specified");
		}

		P2PIP = args[0];
		P2PPort = Integer.parseInt(args[1]);

		firstDHTServerIP = args[2];
		firstDHTServerPort = args[3];

		P2PRMIPort = Integer.parseInt(args[1]);
		P2PRMIPort += 1;

		System.out.println("Application will run  @ " + P2PIP + ":" + P2PPort);

		// Create the RMI on the port specified by the command line + 1
		LocateRegistry.createRegistry(P2PRMIPort);
		Naming.rebind("//" + P2PIP + ":" + P2PRMIPort + "/MyP2PServer", new UI());
		System.out.println("Creating RMI @ " + P2PIP + ":" + P2PRMIPort);






		// Knows the first DHT server
		// AKA "init" interaction
		look_up = (DHTRMIInterface) Naming.lookup("//" + firstDHTServerIP + ":" + firstDHTServerPort + "/MyServer");



		// The main frame
		JFrame frame = new JFrame("P2P Client - @" + P2PIP + ":" + P2PPort);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 400);

		// Execute this block of code when the user quits
		// AKA "exit" interaction
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				// Tell each of the DHT servers to remove entries for every filename in
				// 'fileLocation'
				for (Map.Entry<String, String> value : fileLocation.entrySet()) {
					try {
						look_up.removeFileFromDHT(value.getKey());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		});







		JPanel textArea1 = new JPanel();
		textArea1.setLayout(new BoxLayout(textArea1, BoxLayout.Y_AXIS));
		textArea1.setBorder(BorderFactory.createLineBorder(Color.black));

		JPanel textArea2 = new JPanel();
		textArea2.setLayout(new BoxLayout(textArea2, BoxLayout.Y_AXIS));
		textArea2.setBorder(BorderFactory.createLineBorder(Color.red));


		// Directory panel
		JPanel directoryPanel = new JPanel();

		// Fileserver Button
		JButton startServerButton = new JButton("Start Fileserver");
		directoryPanel.add(startServerButton);




		JButton setDownloadDirectoryButton = new JButton("Download Directory");
		JLabel directoryLabel = new JLabel("Not Set");
		directoryPanel.add(setDownloadDirectoryButton);
		directoryPanel.add(directoryLabel);

		// Download Directory button event
		setDownloadDirectoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser downloadDirectoryChooser = new JFileChooser();
				downloadDirectoryChooser.setCurrentDirectory(new java.io.File("."));
				downloadDirectoryChooser.setDialogTitle("Select");
				downloadDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				downloadDirectoryChooser.showOpenDialog(directoryPanel);
				if (downloadDirectoryChooser.getSelectedFile() == null) {
					return;
				}
				directoryLabel.setText(downloadDirectoryChooser.getSelectedFile().getAbsolutePath());
			}
		});

		// Start Server button event
		startServerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (directoryLabel.getText().isEmpty() || directoryLabel.getText().equals("Not Set")) {
					JOptionPane.showMessageDialog(null,
							"Please choose a destination directory where the file will be saved",
							"Destination Directory Not Set", JOptionPane.WARNING_MESSAGE);
					return;
				}
				// Start the FTP server
				FTPServer fs = new FTPServer(P2PPort, directoryLabel.getText());
				fs.start();
				System.out.println("Fileserver started at port: " + P2PPort);
			}
		});



		JPanel panel = new JPanel();
		JLabel label = new JLabel("File to Get");
		JTextField getFileTextField = new JTextField(20);
		JButton uploadFileButton = new JButton("Upload File");
		JButton getFileButton = new JButton("Get File");
		JButton getAvailableFilesButton = new JButton("Get Available Files");



		// Upload file button event
		// AKA - "inform and update" interaction
		uploadFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Choose a file to upload
					FileDialog dialog = new FileDialog((Frame) null, "Select File to Open");
					dialog.setMode(FileDialog.LOAD);
					dialog.setVisible(true);
					String fullPath = dialog.getDirectory() + dialog.getFile();

					// This is needed for windows directories
					fullPath = fullPath.replace("\\", "\\\\");

					// Add the hosted file to our personal hashtable
					fileLocation.put(dialog.getFile(), fullPath);

					// Update the DHT servers
					look_up.sendToNextNode(dialog.getFile(), P2PIP + ":" + P2PPort);


				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});



		panel.add(uploadFileButton);
		panel.add(getAvailableFilesButton);
		panel.add(getFileButton);


		panel.add(label);
		panel.add(label);
		panel.add(getFileTextField);

		// Get available files button event
		// AKA - "query for content" interaction
		getAvailableFilesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea1.removeAll();
				textArea1.updateUI();
				try {
					Set<String> list = look_up.getAvailableFiles();

					for (String s : list) {
						JLabel label2 = new JLabel();
						label2.setText(s);
						textArea1.add(label2);

					}
					textArea1.updateUI();


				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});

		// Get file button event
		// AKA - "File transfer" interaction
		getFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea2.removeAll();
				textArea2.updateUI();
				try {
					if (getFileTextField == null || getFileTextField.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "Please enter a filename to get", "No Filename Specified!",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					if (directoryLabel.getText().isEmpty() || directoryLabel.getText().equals("Not Set")) {
						JOptionPane.showMessageDialog(null, "The fileserver is not running!",
								"Please Start the Fileserver", JOptionPane.WARNING_MESSAGE);
						return;
					}

					JLabel label = new JLabel();

					String result = look_up.whoOwnsRecord(getFileTextField.getText());
					// 404 error
					if (result.equals("404")) {
						label.setText("404: Not Found");
						textArea2.removeAll();
						textArea2.updateUI();
						textArea2.add(label);
						textArea2.updateUI();
						return;
						// 400 Error
					} else if (result.equals("400")) {
						label.setText("400: Bad Request");
						textArea2.removeAll();
						textArea2.updateUI();
						textArea2.add(label);
						textArea2.updateUI();
						return;
					} else {
						label.setText("The file can be located at " + result);
						textArea2.add(label);
						textArea2.updateUI();
					}




					// Since we know the format of the IP = {ip:port#} we can split the two
					String clientDetails[] = result.split(":");

					// RMI is hosted at Port # + 1
					int serverRMIPort = Integer.parseInt(clientDetails[1]) + 1;
					System.out.println("RMI Server Details: " + clientDetails[0] + ":" + serverRMIPort);

					try {
						// Find the P2P client
						p2pClient = (P2PRMIInterface) Naming
								.lookup("//" + clientDetails[0] + ":" + serverRMIPort + "/MyP2PServer");
					} catch (MalformedURLException | NotBoundException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}


					try {
						// Request the file from the other client
						p2pClient.createConnection(P2PIP, P2PPort, getFileTextField.getText());
						label = new JLabel("200: OK");
						textArea2.removeAll();
						textArea2.updateUI();
						textArea2.add(label);
						textArea2.updateUI();


					} catch (NumberFormatException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}



				} catch (RemoteException e1) {
					e1.printStackTrace();

				}
			}
		});


		// Adding Components to the frame.
		frame.getContentPane().add(BorderLayout.NORTH, directoryPanel);
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		frame.getContentPane().add(BorderLayout.WEST, textArea1);
		frame.getContentPane().add(BorderLayout.EAST, textArea2);

		frame.setVisible(true);
	}


	// Request the file from the other client
	@Override
	public void createConnection(String IP, int port, String filename) throws RemoteException {


		// Create a connection from whoever is calling this method. I.e take their IP
		// and port, establish a connection and send the file
		System.out.println("Person requesting: " + IP + ":" + port);
		System.out.println("File location: " + fileLocation.get(filename));
		FTPClient fTPClient = new FTPClient(IP, port, fileLocation.get(filename));


	}


}



