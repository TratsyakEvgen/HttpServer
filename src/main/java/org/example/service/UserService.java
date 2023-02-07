package org.example.service;

import org.example.entity.User;
import org.example.repository.UserRepository;

import java.net.InetAddress;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = UserRepository.getUsersRepository();
    }

    public void initUser(User user) {
        userRepository.getUsers().put(user.getInetAddress(), user);
    }

    public Optional<User> findUser (InetAddress inetAddress){
       return Optional.ofNullable(userRepository.getUsers().get(inetAddress));
    }
}
