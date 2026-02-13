package me.ifmo.backend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.AOP.LogExecution;
import me.ifmo.backend.AOP.ProfileImport;
import me.ifmo.backend.DTO.ImportOperationDTO;
import me.ifmo.backend.entities.ImportOperation;
import me.ifmo.backend.repositories.ImportOperationRepository;
import me.ifmo.backend.services.ImportService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private final ImportOperationRepository importOperationRepository;
    private final ImportTransactionService importTransactionService;

    @Override
    @LogExecution
    @ProfileImport
    public ImportOperationDTO importRoutes(MultipartFile file, String username) throws IOException {

        ImportOperation op = importTransactionService.createOperation(username, "route");

        try {
            int count = importTransactionService.importRoutes(file);
            importTransactionService.markSuccess(op.getId(), count);
        } catch (RuntimeException exception) {
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
                .build();
    }
}
