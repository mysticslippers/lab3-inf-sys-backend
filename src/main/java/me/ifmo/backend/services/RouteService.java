package me.ifmo.backend.services;

import me.ifmo.backend.DTO.RouteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface RouteService {
    Page<RouteDTO> getAllRoutes(Pageable pageable);
    RouteDTO getById(Long id);
    RouteDTO create(RouteDTO dto);
    RouteDTO update(Long id, RouteDTO dto);
    void delete(Long id);

    RouteDTO findMinDistance();
    Map<Double, Long> groupByRating();
    List<Double> getUniqueRatings();
    List<RouteDTO> findRoutesBetween(Long fromId, Long toId, String sortBy);
    RouteDTO addRouteBetween(Long fromId, Long toId, RouteDTO dto);
}