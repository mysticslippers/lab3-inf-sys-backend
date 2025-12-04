package me.ifmo.backend.mappers;

import me.ifmo.backend.DTO.CoordinatesDTO;
import me.ifmo.backend.DTO.LocationDTO;
import me.ifmo.backend.DTO.RouteDTO;
import me.ifmo.backend.entities.Coordinates;
import me.ifmo.backend.entities.Location;
import me.ifmo.backend.entities.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    RouteDTO toDto(Route route);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    Route toEntity(RouteDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    void updateEntityFromDto(RouteDTO dto, @MappingTarget Route entity);

    CoordinatesDTO toDto(Coordinates coordinates);

    @Mapping(target = "id", ignore = true)
    Coordinates toEntity(CoordinatesDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(CoordinatesDTO dto, @MappingTarget Coordinates entity);

    LocationDTO toDto(Location location);

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(LocationDTO dto, @MappingTarget Location entity);
}
