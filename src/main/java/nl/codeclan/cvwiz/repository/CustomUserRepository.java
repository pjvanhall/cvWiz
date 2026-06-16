package nl.codeclan.cvwiz.repository;

import nl.codeclan.cvwiz.model.CustomUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomUserRepository extends JpaRepository<CustomUser, String> {
}
