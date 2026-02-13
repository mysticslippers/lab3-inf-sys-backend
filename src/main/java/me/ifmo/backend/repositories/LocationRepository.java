package me.ifmo.backend.repositories;

import me.ifmo.backend.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    boolean existsByXAndYAndZ(Long x, Long y, Double z);

    List<Location> findAllByXAndYAndZ(Long x, Long y, Double z);
}
