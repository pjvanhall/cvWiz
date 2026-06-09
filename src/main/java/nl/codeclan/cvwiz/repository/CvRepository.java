package nl.codeclan.cvwiz.repository;


import nl.codeclan.cvwiz.model.Cv;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvRepository extends JpaRepository<Cv, Long> {
}
