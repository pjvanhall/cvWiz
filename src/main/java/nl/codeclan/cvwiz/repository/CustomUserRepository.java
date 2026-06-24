package nl.codeclan.cvwiz.repository;

import nl.codeclan.cvwiz.model.CustomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomUserRepository extends JpaRepository<CustomUser, String> {
    Optional<CustomUser> findByEmailIgnoreCase(String email);
}
