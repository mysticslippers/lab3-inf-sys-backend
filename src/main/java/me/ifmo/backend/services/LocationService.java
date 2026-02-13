package me.ifmo.backend.services;

import me.ifmo.backend.DTO.LocationDTO;

import java.util.List;

public interface LocationService {
    List<LocationDTO> getAllLocations();
    LocationDTO getById(Long id);
    LocationDTO create(LocationDTO dto);
    LocationDTO update(Long id, LocationDTO dto);
    void delete(Long id);
}