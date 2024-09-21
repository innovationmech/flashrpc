package org.innovationmech.flashrpc.example.common.service;

import org.innovationmech.flashrpc.example.proto.UserProto.GetUserRequest;
import org.innovationmech.flashrpc.example.proto.UserProto.GetUserResponse;
import org.innovationmech.flashrpc.example.proto.UserProto.CreateUserRequest;
import org.innovationmech.flashrpc.example.proto.UserProto.CreateUserResponse;

public interface UserService {
    GetUserResponse getUser(GetUserRequest request);
    CreateUserResponse createUser(CreateUserRequest request);
}