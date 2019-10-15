import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class FTPClient {

	private Socket socket;

	public FTPClient(String host, int port, String file) {
		try {
			socket = new Socket(host, port);
			sendFile(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendFile(String file) throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buffer = new byte[8192];

		int read = 0;

		while ((read = fileInputStream.read(buffer)) > 0) {
			dataOutputStream.write(buffer, 0, read);
			dataOutputStream.flush();

		}
		fileInputStream.close();
		dataOutputStream.close();
	}

}
