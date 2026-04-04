package com.finance.service;

import com.finance.dto.request.RegisterRequest;
import com.finance.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, RegisterRequest request);

    void deleteUser(Long id);

    UserResponse getCurrentUser(String username);

}