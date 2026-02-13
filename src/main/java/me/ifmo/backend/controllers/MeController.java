package me.ifmo.backend.controllers;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.UserDTO;
import me.ifmo.backend.entities.User;
import me.ifmo.backend.mappers.UserMapper;
import me.ifmo.backend.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Principal principal) {
        String username = principal.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Current user not found in database: " + username
                ));

        UserDTO dto = userMapper.toDto(user);
        return ResponseEntity.ok(dto);
    }
}
