package me.ifmo.backend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.UserDTO;
import me.ifmo.backend.entities.User;
import me.ifmo.backend.entities.UserRole;
import me.ifmo.backend.mappers.UserMapper;
import me.ifmo.backend.repositories.UserRepository;
import me.ifmo.backend.services.UserManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDTO updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found with id = " + userId));

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown role: " + role +
                    ". Allowed: USER, ADMIN");
        }

        user.setRole(newRole);
        User saved = userRepository.save(user);

        return userMapper.toDto(saved);
    }
}
