package Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class Server {
  private static ConcurrentHashMap<String, UserData> usersMap = new ConcurrentHashMap<>();

  public static void main(String[] args) {
      try {
          ServerSocket serverSocket = new ServerSocket(12345);
          System.out.println("Server is running and waiting for connections...");

          while (true) {
              Socket clientSocket = serverSocket.accept();
              System.out.println("New client connected: " + clientSocket);

              ClientHandler clientHandler = new ClientHandler(clientSocket);
              Thread thread = new Thread(clientHandler);
              thread.start();
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

  public static ConcurrentHashMap<String, UserData> getUsersMap() {
      return usersMap;
  }

  public static List<String> getActiveUsers() {
      List<String> activeUsers = new ArrayList<>();
      for (UserData user : usersMap.values()) {
          if (user.isOnline()) {
              activeUsers.add(user.getUserId());
          }
      }
      return activeUsers;
  }
}

class UserData {

  private String userId;
  private boolean isOnline;

  public UserData(String userId, boolean isOnline) {
    this.userId = userId;
    this.isOnline = isOnline;
  }

  public String getUserId() {
    return userId;
  }

  public boolean isOnline() {
    return isOnline;
  }

  public void setOnline(boolean online) {
    isOnline = online;
  }
}

class ClientHandler implements Runnable {

  private Socket clientSocket;
  private BufferedReader reader;
  private PrintWriter writer;

  public ClientHandler(Socket socket) {
    try {
      this.clientSocket = socket;
      this.reader =
        new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream())
        );
      this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    try {
      String inputLine;
      while ((inputLine = reader.readLine()) != null) {
        // Xử lý yêu cầu từ client
        String[] request = inputLine.split("/");
        if (request.length > 0) {
          String command = request[0];
          switch (command) {
            case "login":
              if (request.length > 1) {
                String username = request[1];
                // Xử lý đăng nhập ở đây (ví dụ: kiểm tra thông tin đăng nhập và gửi phản hồi)
                writer.println("200 LOGIN OK");
              } else {
                writer.println("400 Bad Request");
              }
              break;
            case "message":
              if (request.length > 3) {
                String fromUserID = request[1];
                String receiverID = request[2];
                String messageContent = request[3];
                // Xử lý tin nhắn và gửi lại phản hồi
                // Ví dụ: gửi tin nhắn đến receiverID và trả về phản hồi
                String response =
                  "200 " + messageContent + " FROM " + fromUserID;
                writer.println(response);
              } else {
                writer.println("400 Bad Request");
              }
              break;
            default:
              writer.println("400 Bad Request");
              break;
          }
        }
      }
      clientSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
