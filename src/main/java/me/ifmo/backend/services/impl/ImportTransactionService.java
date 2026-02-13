package me.ifmo.backend.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.RouteDTO;
import me.ifmo.backend.entities.ImportOperation;
import me.ifmo.backend.entities.ImportStatus;
import me.ifmo.backend.repositories.ImportOperationRepository;
import me.ifmo.backend.services.RouteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportTransactionService {

    private final ImportOperationRepository importOperationRepository;
    private final RouteService routeService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportOperation createOperation(String username, String objectType) {
        ImportOperation op = new ImportOperation();
        op.setUsername(username);
        op.setObjectType(objectType);
        op.setStatus(ImportStatus.IN_PROGRESS);
        op.setStartedAt(LocalDateTime.now());
        return importOperationRepository.save(op);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(Long opId, int count) {
        ImportOperation op = importOperationRepository.findById(opId)
                .orElseThrow(() -> new EntityNotFoundException("Import operation not found: " + opId));
        op.setStatus(ImportStatus.SUCCESS);
        op.setImportedCount(count);
        op.setFinishedAt(LocalDateTime.now());
        importOperationRepository.save(op);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long opId, String error) {
        ImportOperation op = importOperationRepository.findById(opId)
                .orElseThrow(() -> new EntityNotFoundException("Import operation not found: " + opId));
        op.setStatus(ImportStatus.FAILED);
        op.setErrorMessage(error);
        op.setFinishedAt(LocalDateTime.now());
        importOperationRepository.save(op);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int importRoutes(MultipartFile file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<RouteDTO> dtoList = mapper.readValue(
                file.getInputStream(),
                new TypeReference<>() {}
        );

        if (dtoList.isEmpty()) {
            throw new IllegalArgumentException("Import file is empty");
        }

        int index = 0;
        int imported = 0;

        for (RouteDTO dto : dtoList) {
            index++;

            try {
                validateForCreate(dto, index);
                
                routeService.create(dto);

                imported++;
            } catch (IllegalArgumentException | IllegalStateException exception) {
                throw new IllegalStateException(
                        "Ошибка при обработке маршрута #" + index + ": " + exception.getMessage(),
                        exception
                );
            }
        }

        return imported;
    }

    private void validateForCreate(RouteDTO dto, int index) {
        if (dto == null) {
            throw new IllegalArgumentException("Route at index " + index + " is null");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Route.name must not be blank at index " + index);
        }
        if (dto.getDistance() == null || dto.getDistance() <= 1.0f) {
            throw new IllegalArgumentException("Route.distance must be > 1 at index " + index);
        }
        if (dto.getRating() == null || dto.getRating() <= 0.0) {
            throw new IllegalArgumentException("Route.rating must be > 0 at index " + index);
        }
        if (dto.getCoordinates() == null) {
            throw new IllegalArgumentException("Route.coordinates must not be null at index " + index);
        }
        if (dto.getFrom() == null) {
            throw new IllegalArgumentException("Route.from must not be null at index " + index);
        }
        if (dto.getTo() == null) {
            throw new IllegalArgumentException("Route.to must not be null at index " + index);
        }
    }
}
