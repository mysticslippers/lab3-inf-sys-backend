package me.ifmo.backend.repositories;

import me.ifmo.backend.entities.ImportOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {

    List<ImportOperation> findByUsernameOrderByStartedAtDesc(String username);

    List<ImportOperation> findAllByOrderByStartedAtDesc();
}
