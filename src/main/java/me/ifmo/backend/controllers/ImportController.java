package me.ifmo.backend.controllers;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.ImportOperationDTO;
import me.ifmo.backend.services.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping("/routes")
    public ResponseEntity<ImportOperationDTO> importRoutes(
            @RequestParam("file") MultipartFile file,
            Principal principal
    ) throws IOException {
        String username = principal.getName();
        ImportOperationDTO dto = importService.importRoutes(file, username);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/operations/my")
    public ResponseEntity<List<ImportOperationDTO>> getMyOperations(Principal principal) {
        String username = principal.getName();
        return ResponseEntity.ok(importService.getMyOperations(username));
    }

    @GetMapping("/operations/all")
    public ResponseEntity<List<ImportOperationDTO>> getAllOperations() {
        return ResponseEntity.ok(importService.getAllOperations());
    }
}
