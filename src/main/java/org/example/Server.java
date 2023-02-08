package org.example;

import org.example.entity.User;
import org.example.service.SocketService;
import org.example.service.UserService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            UserService userService = new UserService();
            while (true) {
                Socket socket = serverSocket.accept();
                InetAddress inetAddress = socket.getInetAddress();
                Optional<User> optionalUser = userService.findUser(inetAddress);
                User user = optionalUser.orElse(new User(inetAddress, ""));
                new Thread(new SocketService(socket, user)).start();
            }
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            System.out.println(e.getMessage());
        }
    }
}