import java.io.*;
import java.net.Socket;

public class Client {

  public static void main(String[] args) {
    try {
      String serverIP = "127.0.0.1"; // IP address of the server
      int serverPort = 12345; // Port number
      Socket socket = new Socket(serverIP, serverPort);

      // Create input and output streams
      DataInputStream in = new DataInputStream(socket.getInputStream());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      // Input the file name
      String fileName = "example.txt"; // Change this to the file name you want to download
      out.writeUTF(fileName);

      // Receive the file name and size from the server
      String receivedFileName = in.readUTF();
      long fileSize = in.readLong();
      System.out.println(
        "Received file: " + receivedFileName + ", Size: " + fileSize + " bytes"
      );

      // Receive and save the file content
      FileOutputStream fileOutputStream = new FileOutputStream(
        receivedFileName
      );
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        fileOutputStream.write(buffer, 0, bytesRead);
      }

      fileOutputStream.close();
      socket.close();
      System.out.println("File downloaded successfully.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
