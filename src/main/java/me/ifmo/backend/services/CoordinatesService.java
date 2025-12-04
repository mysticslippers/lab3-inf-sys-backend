package me.ifmo.backend.services;

import me.ifmo.backend.DTO.CoordinatesDTO;

import java.util.List;

public interface CoordinatesService {
    List<CoordinatesDTO> getAllCoordinates();
    CoordinatesDTO getById(Long id);
    CoordinatesDTO create(CoordinatesDTO dto);
    CoordinatesDTO update(Long id, CoordinatesDTO dto);
    void delete(Long id);
}