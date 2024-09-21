package org.innovationmech.flashrpc.example.server.service.impl;

import org.innovationmech.flashrpc.example.common.service.UserService;
import org.innovationmech.flashrpc.example.proto.UserProto.User;
import org.innovationmech.flashrpc.example.proto.UserProto.GetUserRequest;
import org.innovationmech.flashrpc.example.proto.UserProto.GetUserResponse;
import org.innovationmech.flashrpc.example.proto.UserProto.CreateUserRequest;
import org.innovationmech.flashrpc.example.proto.UserProto.CreateUserResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserServiceImpl implements UserService {
    private final ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public GetUserResponse getUser(GetUserRequest request) {
        User user = users.get(request.getUserId());
        if (user == null) {
            user = User.newBuilder()
                .setId(request.getUserId())
                .setName("未知用户")
                .setEmail("unknown@example.com")
                .build();
        }
        return GetUserResponse.newBuilder().setUser(user).build();
    }

    @Override
    public CreateUserResponse createUser(CreateUserRequest request) {
        int userId = idGenerator.getAndIncrement();
        User newUser = User.newBuilder()
            .setId(userId)
            .setName(request.getName())
            .setEmail(request.getEmail())
            .build();
        users.put(userId, newUser);
        return CreateUserResponse.newBuilder().setUser(newUser).build();
    }
}