package nl.codeclan.cvwiz.repository;


import nl.codeclan.cvwiz.model.Experience;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface ExperienceRepository extends JpaRepositoryImplementation<Experience, Long> {
}
