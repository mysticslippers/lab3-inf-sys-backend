package me.ifmo.backend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.AOP.LogExecution;
import me.ifmo.backend.AOP.ProfileImport;
import me.ifmo.backend.DTO.ImportOperationDTO;
import me.ifmo.backend.entities.ImportOperation;
import me.ifmo.backend.repositories.ImportOperationRepository;
import me.ifmo.backend.services.ImportService;
import me.ifmo.backend.storage.ObjectStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private final ImportOperationRepository importOperationRepository;
    private final ImportTransactionService importTransactionService;
    private final ObjectStorageService objectStorageService;

    @Override
    @LogExecution
    @ProfileImport
    public ImportOperationDTO importRoutes(MultipartFile file, String username) throws IOException {

        ImportOperation op = importTransactionService.createOperation(username, "route");

        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "import.json");
        String contentType = Objects.requireNonNullElse(file.getContentType(), "application/octet-stream");
        long sizeBytes = file.getSize();

        String objectKey = "imports/%d/%s-%s".formatted(
                op.getId(),
                UUID.randomUUID(),
                sanitizeFilename(originalName)
        );

        importTransactionService.attachFileMeta(op.getId(), objectKey, originalName, contentType, sizeBytes);

        try {
            objectStorageService.putObject(
                    objectKey,
                    file.getInputStream(),
                    sizeBytes,
                    contentType
            );
        } catch (RuntimeException exception) {
            importTransactionService.markFailed(op.getId(), "MinIO upload failed: " + exception.getMessage());
            throw exception;
        }

        try {
            int count = importTransactionService.importRoutes(file);
            importTransactionService.markSuccess(op.getId(), count);
        } catch (RuntimeException exception) {
            try {
                objectStorageService.removeObject(objectKey);
            } catch (RuntimeException ignored) {
            }
            importTransactionService.markFailed(op.getId(), exception.getMessage());
            throw exception;
        }

        ImportOperation fresh = importOperationRepository.findById(op.getId())
                .orElseThrow(() -> new EntityNotFoundException("Import operation not found: " + op.getId()));

        return toDto(fresh);
    }

    @Override
    public List<ImportOperationDTO> getMyOperations(String username) {
        return importOperationRepository.findByUsernameOrderByStartedAtDesc(username)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<ImportOperationDTO> getAllOperations() {
        return importOperationRepository.findAllByOrderByStartedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ImportOperationDTO toDto(ImportOperation op) {
        return ImportOperationDTO.builder()
                .id(op.getId())
                .username(op.getUsername())
                .objectType(op.getObjectType())
                .status(op.getStatus().name())
                .importedCount(op.getImportedCount())
                .errorMessage(op.getErrorMessage())
                .startedAt(op.getStartedAt())
                .finishedAt(op.getFinishedAt())
                .fileObjectKey(op.getFileObjectKey())
                .fileOriginalName(op.getFileOriginalName())
                .fileContentType(op.getFileContentType())
                .fileSizeBytes(op.getFileSizeBytes())
                .build();
    }

    private static String sanitizeFilename(String name) {
        String cleaned = name.replace("\\", "_").replace("/", "_");
        cleaned = cleaned.replace("..", "_");
        return cleaned;
    }
}
