package org.example;

import org.example.entity.EntitySocket;
import org.example.service.SocketService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new SocketService(new EntitySocket(socket))).start();
            }
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            System.out.println(e.getMessage());
        }
    }
}