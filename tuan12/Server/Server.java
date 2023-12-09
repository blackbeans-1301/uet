package Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

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

  private static List<UserData> users = new ArrayList<>();

  public static List<UserData> getCurrentUsers() {
    return users;
  }

  public static List<String> getActiveUsers() {
    List<String> activeUsers = new ArrayList<>();
    for (UserData user : users) {
      if (user.isOnline()) {
        activeUsers.add(user.getUserId());
      }
    }
    return activeUsers;
  }

  public static boolean isUserExisted(String username) {
    for (UserData user : users) {
      if (user.getUserId().equals(username)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isUserOnline(String username) {
    for (UserData user : users) {
      if (user.getUserId().equals(username)) {
        return user.isOnline();
      }
    }
    return false;
  }

  public static void setUserStatus(String username, boolean isOnline) {
    for (UserData user : users) {
      if (user.getUserId().equals(username)) {
        user.setOnline(isOnline);
      }
    }
  }

  public static void addUser(String username, Socket clientSocket) {
    users.add(new UserData(username, true, clientSocket));
  }

  public static void setUserSocket(String username, Socket socket) {
    for (UserData user : users) {
      if (user.getUserId().equals(username)) {
        user.setUserSocket(socket);
      }
    }
  }

  public static void sendMessage(String sender, String receiver, String message) {
    for (UserData user : users) {
      if (user.getUserId().equals(receiver)) {
        PrintWriter writer = user.createWriter();
        writer.println("200 " + message + " FROM " + sender);
      }
    }
  }
}

class UserData {
  private String userId;
  private Socket socket;
  private boolean isOnline;

  public UserData(String userId, boolean isOnline, Socket clientSocket) {
    this.userId = userId;
    this.isOnline = isOnline;
    this.socket = clientSocket;
  }

  public PrintWriter createWriter() {
    try {
      PrintWriter writer = new PrintWriter(this.socket.getOutputStream(), true);
      return writer;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
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

  public void setUserSocket(Socket userSocket) {
    this.socket = userSocket;
  }
}

class ClientHandler implements Runnable {
  private Socket clientSocket;
  private BufferedReader reader;
  private PrintWriter writer;
  private String username;

  public ClientHandler(Socket socket) {
    try {
      this.clientSocket = socket;
      this.reader = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream()));
      this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    try {
      while (true) {
        String clientRequest;
        clientRequest = reader.readLine();

        // Xử lý yêu cầu từ client
        String[] request = clientRequest.split("/");
        if (request.length > 0) {
          String command = request[0];
          switch (command) {
            case "login":
              if (request.length > 1) {
                String reqUsername = request[1];

                if (Server.isUserExisted(reqUsername)) {
                  if (Server.isUserOnline(reqUsername)) {
                    writer.println("400 User is already logged in");
                    break;
                  } else {
                    Server.setUserStatus(reqUsername, true);
                  }
                } else {
                  Server.addUser(reqUsername, this.clientSocket);
                }

                this.username = reqUsername;

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
                String response = "200 " + messageContent + " FROM " + fromUserID;
                writer.println(response);
              } else {
                writer.println("400 Bad Request");
              }
              break;
            case "logout":
              Server.setUserStatus(this.username, false);
              writer.println("200 LOGOUT OK");
              clientSocket.close();

              break;
            default:
              writer.println("400 Bad Request");
              break;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
