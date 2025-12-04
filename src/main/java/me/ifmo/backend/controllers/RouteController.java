package me.ifmo.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.RouteDTO;
import me.ifmo.backend.services.RouteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<Page<RouteDTO>> getAllRoutes(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<RouteDTO> routes = routeService.getAllRoutes(PageRequest.of(page, size));
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteDTO> getById(@PathVariable Long id) {
        RouteDTO dto = routeService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<RouteDTO> create(@Valid @RequestBody RouteDTO dto) {
        RouteDTO created = routeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteDTO> update(@PathVariable Long id, @Valid @RequestBody RouteDTO dto) {
        RouteDTO updated = routeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/min-distance")
    public ResponseEntity<RouteDTO> getRouteWithMinDistance() {
        return ResponseEntity.ok(routeService.findMinDistance());
    }

    @GetMapping("/group-by-rating")
    public ResponseEntity<Map<Double, Long>> groupByRating() {
        return ResponseEntity.ok(routeService.groupByRating());
    }

    @GetMapping("/unique-ratings")
    public ResponseEntity<List<Double>> getUniqueRatings() {
        return ResponseEntity.ok(routeService.getUniqueRatings());
    }

    @GetMapping("/between")
    public ResponseEntity<List<RouteDTO>> findRoutesBetween(@RequestParam Long fromId, @RequestParam Long toId, @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(routeService.findRoutesBetween(fromId, toId, sortBy));
    }

    @PostMapping("/between")
    public ResponseEntity<RouteDTO> addRouteBetween(@RequestParam Long fromId, @RequestParam Long toId, @Valid @RequestBody RouteDTO dto) {
        RouteDTO created = routeService.addRouteBetween(fromId, toId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
