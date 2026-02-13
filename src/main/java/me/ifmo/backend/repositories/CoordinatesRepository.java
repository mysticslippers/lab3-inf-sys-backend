package me.ifmo.backend.repositories;

import me.ifmo.backend.entities.Coordinates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoordinatesRepository extends JpaRepository<Coordinates, Long> {

    boolean existsByXAndY(Double x, Float y);

    List<Coordinates> findAllByXAndY(Double x, Float y);
}
