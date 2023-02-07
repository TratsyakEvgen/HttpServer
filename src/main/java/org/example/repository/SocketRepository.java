package org.example.repository;

import org.example.entity.EntitySocket;
import org.example.entity.User;
import org.example.service.UserService;

import java.util.Optional;

public class SocketRepository {
    private final EntitySocket entitySocket;

    public SocketRepository(EntitySocket entitySocket) {
        this.entitySocket = entitySocket;
        initSocket();
    }

    public EntitySocket getEntitySocket() {
        return entitySocket;
    }

    private void initSocket() {
        Optional<User> optionalUser = new UserService().findUser(entitySocket.getInetAddress());
        User user = optionalUser.orElse(new User(entitySocket.getInetAddress(), ""));
        entitySocket.setUser(user);
        new UserService().initUser(user);
    }

}
