package org.example.repository;

import org.example.entity.User;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private static UserRepository userRepository;
    private final Map<InetAddress, User> users = new HashMap<>();

    private UserRepository() {
    }

    public Map<InetAddress, User> getUsers() {
        return users;
    }

    public static UserRepository getUsersRepository() {
        if (userRepository == null) {
            return new UserRepository();
        }
        return userRepository;
    }

}
