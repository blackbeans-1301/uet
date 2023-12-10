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

  public static String getActiveUsers() {
    List<String> activeUsers = new ArrayList<>();
    for (UserData user : users) {
      if (user.isOnline()) {
        activeUsers.add(user.getUserId());
      }
    }

    String activeUsersByString = "[" + String.join(", ", activeUsers) + "]";
    System.out.println("Active users: " + activeUsersByString);

    return activeUsersByString;
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

  public static boolean sendMessage(String sender, String receiver, String message) {
    for (UserData user : users) {
      if (user.getUserId().equals(receiver)) {
        try {
          DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
          out.writeUTF("message:200: Send" + message + " FROM " + sender);
          return true;
        } catch (Exception e) {
          return false;
        }
      }
    }
    return false;
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

  public Socket getSocket() {
    return socket;
  }
}

class ClientHandler implements Runnable {
  private Socket clientSocket;
  private DataInputStream in;
  private DataOutputStream out;
  private String username;

  public ClientHandler(Socket socket) {
    try {
      this.clientSocket = socket;
      this.in = new DataInputStream(clientSocket.getInputStream());
      this.out = new DataOutputStream(clientSocket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    System.out.println("Start handle client request...");

    try {
      while (true) {
        String clientRequest;
        clientRequest = in.readUTF();
        System.out.println("Client request: " + clientRequest);

        // Xử lý yêu cầu từ client
        String[] request = clientRequest.split("/");
        if (request.length > 0) {
          String command = request[0];
          switch (command) {
            case "login":
              if (request.length > 1) {
                String reqUsername = request[1];
                String activeUsers = Server.getActiveUsers();

                if (Server.isUserExisted(reqUsername)) {
                  if (Server.isUserOnline(reqUsername)) {
                    out.writeUTF("login:400: User is already logged in");
                    break;
                  } else {
                    Server.setUserStatus(reqUsername, true);
                  }
                } else {
                  Server.addUser(reqUsername, this.clientSocket);
                  out.writeUTF("login:200: signed up a new user and login OK, active users: " + activeUsers);
                }

                this.username = reqUsername;

              } else {
                out.writeUTF("login:400: Bad Request");
              }
              break;
            case "message":
              if (request.length > 3) {
                String fromUserID = request[1];
                String receiverID = request[2];
                String messageContent = request[3];

                boolean sendSuccessfully = Server.sendMessage(fromUserID, receiverID, messageContent);

                String responseToClient = "";
                if (sendSuccessfully) {
                  responseToClient = "message:200: send message to" + receiverID + " successfully";
                } else {
                  responseToClient = "message:200: send message to " + receiverID
                      + " error, user is offline or not existed";
                }
                out.writeUTF(responseToClient);
              } else {
                out.writeUTF("message:400: Bad Request");
              }
              break;
            case "logout":
              Server.setUserStatus(this.username, false);
              out.writeUTF("logout:200: LOGOUT OK");
              clientSocket.close();

              break;
            default:
              out.writeUTF("command:200: Bad Request");
              break;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
