import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
  public static boolean isLoggedIn = false;
  public static String username = "";

  public static void main(String[] args) {
    try {
      final int serverPort = 12345; // Port number
      Scanner scanner = new Scanner(System.in);
      String inputServerIp;
      Socket socket = null;

      // do {
      // System.out.print("Enter server IP: ");

      // // Read user input
      // inputServerIp = scanner.nextLine().toLowerCase();

      // if (isValidIPAddress(inputServerIp)) {
      // break;
      // } else {
      // System.out.println("Invalid IP address!");
      // }

      // } while (true);

      socket = new Socket("127.0.0.1", serverPort);

      // Create input and output streams
      DataInputStream in = new DataInputStream(socket.getInputStream());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      HandleServerResponse responseHandler = new HandleServerResponse(in);
      Thread thread = new Thread(responseHandler);
      thread.start();

      System.out.println("Connected to the server.");

      while (true) {
        Thread.sleep(500);
        System.out.print("Enter your command(login/logout/message): ");
        String message;
        message = scanner.nextLine().toLowerCase();

        if (message.equals("login")) {
          handleLogin(scanner, in, out);
          continue;
        } else {
          if (!isLoggedIn) {
            System.out.println("You must login first!");
            continue;
          }

          switch (message) {
            case "message":
              handleChat(username, scanner, in, out);
              continue;
            case "logout":
              System.out.println("Logging out...");
              out.writeUTF("logout/" + username);
              System.out.println("Server: " + in.readUTF());
              break;
            default:
              System.out.println("Invalid command!");
              break;
          }
          continue;
        }
      }

    } catch (Exception e) {
    }
  }

  private static void handleChat(String username, Scanner scanner, DataInputStream in, DataOutputStream out) {
    String receiverId = "";
    String message = "";

    System.out.print("Enter receiver ID: ");
    receiverId = scanner.nextLine().toLowerCase();

    System.out.print("Enter message: ");
    message = scanner.nextLine().toLowerCase();

    try {
      out.writeUTF("message/" + username + "/" + receiverId + "/" + message);
    } catch (Exception e) {
    }
  }

  private static void handleLogin(Scanner scanner, DataInputStream in, DataOutputStream out) {
    System.out.print("Enter username: ");
    String username = scanner.nextLine().toLowerCase();

    try {
      out.writeUTF("login/" + username);
    } catch (Exception e) {
    }
  }

  private static boolean isValidIPAddress(String ipAddress) {
    String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(ipAddress);

    return matcher.matches();
  }
}

class HandleServerResponse extends Thread {
  private DataInputStream in;

  public HandleServerResponse(DataInputStream in) {
    this.in = in;
  }

  @Override
  public void run() {
    try {
      while (true) {
        String serverResponse = in.readUTF();
        System.out.println("SERVER: " + serverResponse);

        String[] request = serverResponse.split(":");
        if (request.length > 2) {
          String command = request[0];
          switch (command) {
            case "login":
              if (request.length > 1) {
                String status = request[1];
                if (status.equals("200")) {
                  Client.isLoggedIn = true;
                  Client.username = request[2];
                }
              }
              break;
            case "message":
              if (request.length > 1) {
                String status = request[1];
                if (status.equals("200")) {
                  System.out.println("Message sent successfully!");
                } else {
                  System.out.println("Message sent failed!");
                }
              }
              break;
            case "logout":
              if (request.length > 1) {
                String status = request[1];
                if (status.equals("200")) {
                  Client.isLoggedIn = false;
                  Client.username = "";
                }
              }
              break;
            default:
              break;
          }
        }
      }
    } catch (Exception e) {
    }
  }
}