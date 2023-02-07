package org.example.entity;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class EntitySocket {
    private final Socket socket;
    private final InetAddress inetAddress;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private User user;

    public EntitySocket(Socket socket) throws Throwable {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.inetAddress = socket.getInetAddress();
    }


    public void setUser(User user) {
        this.user = user;
    }

    public Socket getSocket() {
        return socket;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public User getUser() {
        return user;
    }
}
