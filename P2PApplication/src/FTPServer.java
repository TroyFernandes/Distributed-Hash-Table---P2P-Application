import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer extends Thread {

	private ServerSocket serverSocket;
	private String destination;

	public FTPServer(int port, String outputDestination) {
		destination = outputDestination;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				saveFile(clientSocket, destination);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveFile(Socket clientSocket, String outputDestination) throws IOException {
		DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
		System.out.println(outputDestination);
		outputDestination = outputDestination.replace("\\", "\\\\");

		FileOutputStream fileOutputStream = new FileOutputStream(outputDestination + "\\out.PNG");
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		while ((bytesRead = dataInputStream.read(buffer, 0, buffer.length)) != -1) {
			fileOutputStream.write(buffer, 0, bytesRead);
		}

		System.out.println("File Copied");
		fileOutputStream.close();
		dataInputStream.close();
	}


}