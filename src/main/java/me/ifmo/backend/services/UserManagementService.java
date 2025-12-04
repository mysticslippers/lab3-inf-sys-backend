package me.ifmo.backend.services;

import me.ifmo.backend.DTO.UserDTO;

import java.util.List;

public interface UserManagementService {

    List<UserDTO> getAllUsers();

    UserDTO updateUserRole(Long userId, String role);
}
