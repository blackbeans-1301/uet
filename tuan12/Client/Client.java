import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
  static void main(String[] args) {
    String username = "";
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

      System.out.println("Connected to the server.");

      while (true) {
        String message;

        message = scanner.nextLine().toLowerCase();

        if (message.equals("login")) {
          handleLogin(scanner, in, out);
          continue;
        } else {
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
      String response = in.readUTF();

      System.out.println("Server: " + response);
    } catch (Exception e) {
    }
  }

  private static String handleLogin(Scanner scanner, DataInputStream in, DataOutputStream out) {
    System.out.println("Connected to the server.");
    String username = scanner.nextLine().toLowerCase();

    try {
      out.writeUTF("login " + username);
      String response = in.readUTF();

      System.out.println("Server" + response);

      if (response.contains("200")) {
        return username;
      } else {
        return "";
      }
    } catch (Exception e) {
      return "";
    }
  }

  private static boolean isValidIPAddress(String ipAddress) {
    String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(ipAddress);

    return matcher.matches();
  }
}