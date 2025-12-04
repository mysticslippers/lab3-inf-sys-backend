package me.ifmo.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.CoordinatesDTO;
import me.ifmo.backend.services.CoordinatesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coordinates")
@RequiredArgsConstructor
public class CoordinatesController {

    private final CoordinatesService coordinatesService;

    @GetMapping
    public ResponseEntity<List<CoordinatesDTO>> getAll() {
        return ResponseEntity.ok(coordinatesService.getAllCoordinates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoordinatesDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(coordinatesService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CoordinatesDTO> create(@Valid @RequestBody CoordinatesDTO dto) {
        CoordinatesDTO created = coordinatesService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoordinatesDTO> update(@PathVariable Long id, @Valid @RequestBody CoordinatesDTO dto) {
        CoordinatesDTO updated = coordinatesService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coordinatesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
