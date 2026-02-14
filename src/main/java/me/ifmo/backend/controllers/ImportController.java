package me.ifmo.backend.controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.ImportOperationDTO;
import me.ifmo.backend.entities.ImportOperation;
import me.ifmo.backend.repositories.ImportOperationRepository;
import me.ifmo.backend.services.ImportService;
import me.ifmo.backend.storage.ObjectStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    private final ImportOperationRepository importOperationRepository;
    private final ObjectStorageService objectStorageService;

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

    @GetMapping("/operations/{id}/file")
    public ResponseEntity<InputStreamResource> downloadImportFile(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        ImportOperation op = importOperationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Import operation not found: " + id));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        String requester = authentication.getName();

        if (!isAdmin && !Objects.equals(requester, op.getUsername())) {
            return ResponseEntity.status(403).build();
        }

        if (op.getFileObjectKey() == null || op.getFileObjectKey().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        String filename = Objects.requireNonNullElse(op.getFileOriginalName(), "import.json");
        String contentType = Objects.requireNonNullElse(op.getFileContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE);

        InputStreamResource resource = new InputStreamResource(objectStorageService.getObject(op.getFileObjectKey()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename.replace("\"", "") + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
