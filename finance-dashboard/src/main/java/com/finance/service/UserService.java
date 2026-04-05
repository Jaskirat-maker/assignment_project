package com.finance.service;

import com.finance.dto.request.UserUpdateRequest;
import com.finance.dto.response.UserResponse;
import com.finance.entity.enums.Role;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UserUpdateRequest request);

    UserResponse updateUserStatus(Long id, Boolean isActive);

    UserResponse updateUserRole(Long id, Role role);

    void deleteUser(Long id);

    UserResponse getCurrentUser(String username);

}