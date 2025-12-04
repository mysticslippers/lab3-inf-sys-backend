package me.ifmo.backend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.AOP.LogExecution;
import me.ifmo.backend.DTO.CoordinatesDTO;
import me.ifmo.backend.DTO.LocationDTO;
import me.ifmo.backend.DTO.RouteDTO;
import me.ifmo.backend.entities.Coordinates;
import me.ifmo.backend.entities.Location;
import me.ifmo.backend.entities.Route;
import me.ifmo.backend.mappers.RouteMapper;
import me.ifmo.backend.repositories.CoordinatesRepository;
import me.ifmo.backend.repositories.LocationRepository;
import me.ifmo.backend.repositories.RouteRepository;
import me.ifmo.backend.services.RouteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteServiceImpl implements RouteService {

    private static final String TOPIC = "/topic/routes";

    private final RouteRepository routeRepository;
    private final RouteMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final CoordinatesRepository coordinatesRepository;
    private final LocationRepository locationRepository;

    @Override
    public Page<RouteDTO> getAllRoutes(Pageable pageable) {
        return routeRepository.findAll(pageable)
                .map(mapper::toDto);
    }

    @Override
    public RouteDTO getById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Route with id " + id + " not found"));
        return mapper.toDto(route);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @LogExecution
    public RouteDTO create(RouteDTO dto) {
        validateForCreate(dto);

        checkUniqueOnCreate(dto);

        Route route = mapper.toEntity(dto);
        route.setCreationDate(LocalDateTime.now());

        route.setCoordinates(resolveCoordinates(dto.getCoordinates()));
        route.setFrom(resolveLocation(dto.getFrom(), "from"));
        route.setTo(resolveLocation(dto.getTo(), "to"));

        Route saved = routeRepository.save(route);
        RouteDTO result = mapper.toDto(saved);

        messagingTemplate.convertAndSend(
                TOPIC,
                new WebSocketEvent("route", "create", result)
        );

        return result;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @LogExecution
    public RouteDTO update(Long id, RouteDTO dto) {
        validateForCreate(dto);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Route not found with id = " + id));

        mapper.updateEntityFromDto(dto, route);

        route.setCoordinates(resolveCoordinates(dto.getCoordinates()));
        route.setFrom(resolveLocation(dto.getFrom(), "from"));
        route.setTo(resolveLocation(dto.getTo(), "to"));

        Route saved = routeRepository.save(route);
        RouteDTO result = mapper.toDto(saved);

        messagingTemplate.convertAndSend(
                TOPIC,
                new WebSocketEvent("route", "update", result)
        );

        return result;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Route existing = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Route not found with id = " + id));

        routeRepository.delete(existing);

        messagingTemplate.convertAndSend(
                TOPIC,
                new WebSocketEvent("route", "delete", mapper.toDto(existing))
        );
    }

    @Override
    public RouteDTO findMinDistance() {
        Route route = routeRepository.findRouteWithMinDistance()
                .orElseThrow(() -> new EntityNotFoundException("No routes with non-null distance found"));
        return mapper.toDto(route);
    }

    @Override
    public Map<Double, Long> groupByRating() {
        return routeRepository.groupByRating().stream()
                .collect(Collectors.toMap(
                        arr -> (Double) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    @Override
    public List<Double> getUniqueRatings() {
        return routeRepository.findDistinctRatings();
    }

    @Override
    public List<RouteDTO> findRoutesBetween(Long fromId, Long toId, String sortBy) {
        List<Route> routes = routeRepository.findRoutesBetween(fromId, toId);

        String sort = sortBy == null ? "" : sortBy;

        Comparator<Route> comparator = switch (sort) {
            case "distance" ->
                    Comparator.comparing(Route::getDistance, Comparator.nullsLast(Float::compareTo));
            case "rating" ->
                    Comparator.comparing(Route::getRating, Comparator.nullsLast(Double::compareTo));
            case "name" ->
                    Comparator.comparing(Route::getName, Comparator.nullsLast(String::compareTo));
            default ->
                    Comparator.comparing(Route::getId, Comparator.nullsLast(Long::compareTo));
        };

        return routes.stream()
                .sorted(comparator)
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public RouteDTO addRouteBetween(Long fromId, Long toId, RouteDTO dto) {
        validateForCreate(dto);

        Location from = locationRepository.findById(fromId)
                .orElseThrow(() -> new EntityNotFoundException("From location not found: " + fromId));
        Location to = locationRepository.findById(toId)
                .orElseThrow(() -> new EntityNotFoundException("To location not found: " + toId));

        Route route = mapper.toEntity(dto);
        route.setCreationDate(LocalDateTime.now());

        route.setFrom(from);
        route.setTo(to);
        route.setCoordinates(resolveCoordinates(dto.getCoordinates()));

        Route saved = routeRepository.save(route);
        RouteDTO result = mapper.toDto(saved);

        messagingTemplate.convertAndSend(
                TOPIC,
                new WebSocketEvent("route", "create", result)
        );

        return result;
    }

    private Coordinates resolveCoordinates(CoordinatesDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Route.coordinates cannot be null");
        }

        if (dto.getId() != null) {
            return coordinatesRepository.findById(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Coordinates not found: " + dto.getId()));
        }

        if (dto.getX() == null || dto.getY() == null) {
            throw new IllegalArgumentException("Coordinates x and y must be provided");
        }

        List<Coordinates> existingList = coordinatesRepository.findAllByXAndY(dto.getX(), dto.getY());
        if (!existingList.isEmpty()) {
            return existingList.get(0);
        }

        Coordinates coordinates = mapper.toEntity(dto);
        coordinates.setId(null);
        return coordinates;
    }


    private Location resolveLocation(LocationDTO dto, String role) {
        if (dto == null) {
            throw new IllegalArgumentException("Location (" + role + ") cannot be null");
        }

        if (dto.getId() != null) {
            return locationRepository.findById(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Location (" + role + ") not found: " + dto.getId()
                    ));
        }

        Long x = dto.getX();
        Long y = dto.getY();
        Double z = dto.getZ();

        if (y == null || z == null) {
            throw new IllegalArgumentException("Location (" + role + ") must have y and z");
        }

        if (x != null) {
            List<Location> existing = locationRepository.findAllByXAndYAndZ(x, y, z);
            if (!existing.isEmpty()) {
                return existing.get(0);
            }
        }

        Location location = mapper.toEntity(dto);
        location.setId(null);
        return location;
    }

    private void validateForCreate(RouteDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Route cannot be null");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Route.name must not be blank");
        }
        if (dto.getCoordinates() == null) {
            throw new IllegalArgumentException("Route.coordinates cannot be null");
        }
        if (dto.getFrom() == null) {
            throw new IllegalArgumentException("Route.from cannot be null");
        }
        if (dto.getTo() == null) {
            throw new IllegalArgumentException("Route.to cannot be null");
        }
        if (dto.getDistance() == null || dto.getDistance() <= 1.0f) {
            throw new IllegalArgumentException("Route.distance must be greater than 1");
        }
        if (dto.getRating() == null || dto.getRating() <= 0.0) {
            throw new IllegalArgumentException("Route.rating must be greater than 0");
        }
    }

    private void checkUniqueOnCreate(RouteDTO dto) {
        if (dto == null || dto.getFrom() == null || dto.getTo() == null) {
            return;
        }

        String name = dto.getName();

        Long fromId = dto.getFrom().getId();
        Long toId = dto.getTo().getId();

        if (fromId != null && toId != null) {
            if (routeRepository.existsByNameAndFrom_IdAndTo_Id(name, fromId, toId)) {
                throw new IllegalStateException(
                        "Route with name '" + name + "' and endpoints (" + fromId + " -> " + toId + ") already exists"
                );
            }
            return;
        }

        LocationDTO from = dto.getFrom();
        LocationDTO to = dto.getTo();

        if (from.getY() == null || from.getZ() == null || to.getY() == null || to.getZ() == null) {
            return;
        }

        Long fromX = from.getX();
        Long fromY = from.getY();
        Double fromZ = from.getZ();

        Long toX = to.getX();
        Long toY = to.getY();
        Double toZ = to.getZ();

        boolean exists = routeRepository.existsByNameAndFrom_XAndFrom_YAndFrom_ZAndTo_XAndTo_YAndTo_Z(
                name,
                fromX, fromY, fromZ,
                toX, toY, toZ
        );

        if (exists) {
            throw new IllegalStateException(
                    "Route with name '" + name + "' and endpoints (" +
                            "from: [" + fromX + "," + fromY + "," + fromZ + "], " +
                            "to: [" + toX + "," + toY + "," + toZ + "]) already exists"
            );
        }
    }

}
