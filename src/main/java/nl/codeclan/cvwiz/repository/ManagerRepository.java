package nl.codeclan.cvwiz.repository;


import nl.codeclan.cvwiz.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManagerRepository extends JpaRepository<Manager, UUID> {
}
