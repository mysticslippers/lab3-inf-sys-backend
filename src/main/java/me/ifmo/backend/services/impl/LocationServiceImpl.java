package me.ifmo.backend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.AOP.LogExecution;
import me.ifmo.backend.DTO.LocationDTO;
import me.ifmo.backend.entities.Location;
import me.ifmo.backend.mappers.RouteMapper;
import me.ifmo.backend.repositories.LocationRepository;
import me.ifmo.backend.repositories.RouteRepository;
import me.ifmo.backend.services.LocationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private static final String TOPIC = "/topic/locations";

    private final LocationRepository locationRepository;
    private final RouteRepository routeRepository;
    private final RouteMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LocationDTO getById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Location with id " + id + " not found"));
        return mapper.toDto(location);
    }

    @Override
    @Transactional
    @LogExecution
    public LocationDTO create(LocationDTO dto) {
        validate(dto);
        checkUniqueOnCreate(dto);

        Location entity = mapper.toEntity(dto);
        entity.setId(null);

        Location saved = locationRepository.save(entity);
        LocationDTO result = mapper.toDto(saved);

        messagingTemplate.convertAndSend(
                TOPIC,
                new WebSocketEvent("location", "create", result)
        );

        return result;
    }

    @Override
    @Transactional
    @LogExecution
    public LocationDTO update(Long id, LocationDTO dto) {
        validate(dto);
        checkUniqueOnUpdate(id, dto);

        Location existing = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location with id " + id + " not found"));

        mapper.updateEntityFromDto(dto, existing);

        Location saved = locationRepository.save(existing);
        LocationDTO result = mapper.toDto(saved);

        messagingTemplate.convertAndSend(
                TOPIC,
                new WebSocketEvent("location", "update", result)
        );

        return result;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Location existing = locationRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Location with id " + id + " not found"));

        boolean inUse = routeRepository.existsByFrom_IdOrTo_Id(id, id);
        if (inUse) {
            throw new IllegalStateException(
                    "Cannot delete location: it is used in existing routes."
            );
        }

        locationRepository.delete(existing);

        messagingTemplate.convertAndSend(
                TOPIC,
                new WebSocketEvent("location", "delete", mapper.toDto(existing))
        );
    }

    private void validate(LocationDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (dto.getY() == null) {
            throw new IllegalArgumentException("Location.y cannot be null");
        }
        if (dto.getZ() == null) {
            throw new IllegalArgumentException("Location.z cannot be null");
        }
    }

    private void checkUniqueOnCreate(LocationDTO dto) {
        Long x = dto.getX();
        Long y = dto.getY();
        Double z = dto.getZ();

        if (x == null || y == null || z == null) {
            return;
        }

        if (locationRepository.existsByXAndYAndZ(x, y, z)) {
            throw new IllegalStateException(
                    "Location (" + x + ", " + y + ", " + z + ") already exists"
            );
        }
    }

    private void checkUniqueOnUpdate(Long id, LocationDTO dto) {
        List<Location> existingList = locationRepository.findAllByXAndYAndZ(dto.getX(), dto.getY(), dto.getZ());

        for (Location other : existingList) {
            if(!other.getId().equals(id)) {
                throw new IllegalStateException(
                        "Location (" + dto.getX() + ", " + dto.getY() + ", " + dto.getZ() + ") already exists for id=" + other.getId()
                );
            }
        }
    }
}
