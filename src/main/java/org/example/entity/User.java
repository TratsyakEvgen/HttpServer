package org.example.entity;

import java.net.InetAddress;

public class User {

    private final InetAddress inetAddress;
    private String cookie;


    public User(InetAddress inetAddress,  String cookie) {
        this.inetAddress = inetAddress;
        this.cookie = cookie;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }


    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getCookie() {
        return cookie;
    }
}
