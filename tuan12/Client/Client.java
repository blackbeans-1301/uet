package Client;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter server IP: ");
            String serverIP = userInput.readLine();

            Socket socket = new Socket(serverIP, 12345);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            String command;
            while (true) {
                System.out.print("Enter command (login/message/logout): ");
                command = userInput.readLine();

                switch (command) {
                    case "login":
                        System.out.print("Enter username: ");
                        String username = userInput.readLine();
                        writer.println("login/" + username);
                        break;
                    case "message":
                        System.out.print("Enter receiver ID: ");
                        String receiverID = userInput.readLine();
                        System.out.print("Enter message: ");
                        String message = userInput.readLine();
                        writer.println("message/" + receiverID + "/" + message);
                        break;
                    case "logout":
                        // Gửi lệnh logout và kết thúc vòng lặp
                        writer.println("logout");
                        socket.close();
                        return;
                    default:
                        System.out.println("Invalid command");
                        break;
                }

                // Đọc và in ra phản hồi từ máy chủ
                String response = reader.readLine();
                System.out.println("Server response: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
