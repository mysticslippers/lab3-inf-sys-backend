package me.ifmo.backend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.AOP.LogExecution;
import me.ifmo.backend.DTO.CoordinatesDTO;
import me.ifmo.backend.entities.Coordinates;
import me.ifmo.backend.mappers.RouteMapper;
import me.ifmo.backend.repositories.CoordinatesRepository;
import me.ifmo.backend.repositories.RouteRepository;
import me.ifmo.backend.services.CoordinatesService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoordinatesServiceImpl implements CoordinatesService {

    private static final String TOPIC = "/topic/coordinates";

    private final CoordinatesRepository coordinatesRepository;
    private final RouteRepository routeRepository;
    private final RouteMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<CoordinatesDTO> getAllCoordinates() {
        return coordinatesRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CoordinatesDTO getById(Long id) {
        Coordinates entity = coordinatesRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Coordinates with id " + id + " not found"));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    @LogExecution
    public CoordinatesDTO create(CoordinatesDTO dto) {
        validate(dto);
        checkUniqueOnCreate(dto);

        Coordinates entity = mapper.toEntity(dto);
        entity.setId(null);

        Coordinates saved = coordinatesRepository.save(entity);
        CoordinatesDTO result = mapper.toDto(saved);

        messagingTemplate.convertAndSend(
                TOPIC,
                new WebSocketEvent("coordinates", "create", result)
        );

        return result;
    }

    @Override
    @Transactional
    @LogExecution
    public CoordinatesDTO update(Long id, CoordinatesDTO dto) {
        validate(dto);
        checkUniqueOnUpdate(id, dto);

        Coordinates existing = coordinatesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coordinates with id " + id + " not found"));

        mapper.updateEntityFromDto(dto, existing);

        Coordinates saved = coordinatesRepository.save(existing);
        CoordinatesDTO result = mapper.toDto(saved);

        messagingTemplate.convertAndSend(
                TOPIC,
                new WebSocketEvent("coordinates", "update", result)
        );

        return result;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Coordinates existing = coordinatesRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Coordinates with id " + id + " not found"));

        boolean inUse = routeRepository.existsByCoordinates_Id(id);
        if (inUse) {
            throw new IllegalStateException(
                    "Cannot delete coordinates: they are referenced by existing routes."
            );
        }

        coordinatesRepository.delete(existing);

        messagingTemplate.convertAndSend(
                TOPIC,
                new WebSocketEvent("coordinates", "delete", mapper.toDto(existing))
        );
    }

    private void validate(CoordinatesDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        if (dto.getX() == null) {
            throw new IllegalArgumentException("Coordinates.x cannot be null");
        }
        if (dto.getY() == null) {
            throw new IllegalArgumentException("Coordinates.y cannot be null");
        }
        if (dto.getY() <= -976.0f) {
            throw new IllegalArgumentException("Coordinates.y must be greater than -976");
        }
    }

    private void checkUniqueOnCreate(CoordinatesDTO dto) {
        if (coordinatesRepository.existsByXAndY(dto.getX(), dto.getY())) {
            throw new IllegalStateException(
                    "Coordinates (" + dto.getX() + ", " + dto.getY() + ") already exist"
            );
        }
    }

    private void checkUniqueOnUpdate(Long id, CoordinatesDTO dto) {
        List<Coordinates> existingList = coordinatesRepository.findAllByXAndY(dto.getX(), dto.getY());

        for (Coordinates other : existingList) {
            if (!other.getId().equals(id)) {
                throw new IllegalStateException(
                        "Coordinates (" + dto.getX() + ", " + dto.getY() + ") already exist for id=" + other.getId()
                );
            }
        }
    }

}
