package nl.codeclan.cvwiz.repository;


import nl.codeclan.cvwiz.model.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConsultantRepository extends JpaRepository<Consultant, UUID> {
    Optional<Consultant> getByFirstnameAndLastname(String firstname, String lastname);
}
