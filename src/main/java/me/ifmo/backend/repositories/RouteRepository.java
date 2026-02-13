package me.ifmo.backend.repositories;

import me.ifmo.backend.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query("""
           SELECT r FROM Route r
           WHERE r.distance = (
               SELECT MIN(r2.distance)
               FROM Route r2
               WHERE r2.distance IS NOT NULL
           )
           """)
    Optional<Route> findRouteWithMinDistance();

    @Query("SELECT r.rating, COUNT(r) FROM Route r GROUP BY r.rating")
    List<Object[]> groupByRating();

    @Query("SELECT DISTINCT r.rating FROM Route r WHERE r.rating IS NOT NULL")
    List<Double> findDistinctRatings();

    @Query("SELECT r FROM Route r WHERE r.from.id = :fromId AND r.to.id = :toId")
    List<Route> findRoutesBetween(@Param("fromId") Long fromId, @Param("toId") Long toId);

    boolean existsByCoordinates_Id(Long coordinatesId);

    boolean existsByFrom_IdOrTo_Id(Long fromId, Long toId);

    boolean existsByNameAndFrom_IdAndTo_Id(String name, Long fromId, Long toId);

    boolean existsByNameAndFrom_XAndFrom_YAndFrom_ZAndTo_XAndTo_YAndTo_Z(
            String name,
            Long fromX,
            Long fromY,
            Double fromZ,
            Long toX,
            Long toY,
            Double toZ
    );
}
