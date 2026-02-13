package me.ifmo.backend.controllers;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.UpdateUserRoleRequest;
import me.ifmo.backend.DTO.UserDTO;
import me.ifmo.backend.services.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }


    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long id, @RequestBody UpdateUserRoleRequest request) {
        UserDTO updated = userManagementService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(updated);
    }
}
