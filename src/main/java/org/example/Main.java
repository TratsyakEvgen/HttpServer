package org.example;

import org.example.entity.EntitySocket;
import org.example.service.SocketService;

import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new SocketService(new EntitySocket(socket))).start();
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage().toCharArray());
        }
    }
}