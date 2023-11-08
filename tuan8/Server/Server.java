import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

  public static void main(String[] args) {
    try {
      ServerSocket serverSocket = new ServerSocket(12345); // Port number

      while (true) {
        System.out.println("Waiting for a client to connect...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected!");

        // Create input and output streams
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(
          clientSocket.getOutputStream()
        );

        // Read file name and size from the client
        String fileName = in.readUTF();
        long fileSize = in.readLong();
        System.out.println(
          "Received file: " + fileName + ", Size: " + fileSize + " bytes"
        );

        // Send the file name and size to the client
        out.writeUTF(fileName);
        out.writeLong(fileSize);

        // Send the file content to the client
        FileInputStream fileInputStream = new FileInputStream(fileName);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }

        fileInputStream.close();
        clientSocket.close();
        System.out.println("File sent to the client.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
