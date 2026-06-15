package nl.codeclan.cvwiz;

import jakarta.persistence.EntityNotFoundException;
import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.repository.ExperienceRepository;
import nl.codeclan.cvwiz.service.ExperienceService;
import nl.codeclan.cvwiz.support.RepositoryDoubles;
import nl.codeclan.cvwiz.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExperienceServiceTest {

    private RepositoryDoubles.TestRepository<ExperienceRepository, nl.codeclan.cvwiz.model.Experience, Long> repository;
    private ExperienceService service;

    @BeforeEach
    void setUp() {
        repository = RepositoryDoubles.experiences();
        service = new ExperienceService(repository.repository());
    }

    @Test
    void createsDefaultExperienceWhenDtoIsNull() {
        ErvaringDto result = service.createExperience(null);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(repository.savedEntities()).hasSize(1);
    }

    @Test
    void createsExperienceAndAssignsNextId() {
        repository.put(TestData.experience(1L));
        ErvaringDto input = TestData.experienceDto(99L);

        ErvaringDto result = service.createExperience(input);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getBedrijf()).isEqualTo("CodeClan");
        assertThat(input.getId()).isEqualTo(2L);
    }

    @Test
    void createsExperienceWithNextFreeIdWhenCountBasedIdExists() {
        repository.put(TestData.experience(2L));
        ErvaringDto input = TestData.experienceDto(99L);

        ErvaringDto result = service.createExperience(input);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(repository.find(3L)).isPresent();
    }

    @Test
    void createsExperienceList() {
        List<ErvaringDto> result = service.createExperienceList(List.of(TestData.experienceDto(10L), TestData.experienceDto(11L)));

        assertThat(result).extracting(ErvaringDto::getId).containsExactly(1L, 2L);
        assertThat(repository.savedEntities()).hasSize(2);
    }

    @Test
    void updatesSingleAndMultipleExperiences() {
        ErvaringDto first = TestData.experienceDto(1L);
        ErvaringDto second = TestData.experienceDto(2L);

        service.updateExperience(first);
        service.updateExperiencesList(List.of(first, second));

        assertThat(repository.savedEntities()).hasSize(3);
        assertThat(repository.find(2L)).isPresent();
    }

    @Test
    void ignoresNullUpdateInputs() {
        assertThat(service.updateExperience(null)).isNull();
        assertThat(service.saveSubmittedExperience(null)).isNull();

        service.updateExperiencesList(null);

        assertThat(repository.savedEntities()).isEmpty();
    }

    @Test
    void returnsEmptyListsForNullExperienceLists() {
        assertThat(service.createExperienceList(null)).isEmpty();
        assertThat(service.saveSubmittedExperiencesList(null)).isEmpty();
    }

    @Test
    void savesSubmittedNewExperienceWithGeneratedId() {
        ErvaringDto input = new ErvaringDto(null, "New", "2025", "Lead", "IT", "Kotlin", "Led", "Delivered");

        ErvaringDto result = service.saveSubmittedExperience(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(input.getId()).isEqualTo(1L);
        assertThat(repository.find(1L)).isPresent();
    }

    @Test
    void savesSubmittedExistingExperienceAsUpdate() {
        repository.put(TestData.experience(1L));
        ErvaringDto input = new ErvaringDto(1L, "Updated", "2026", "Lead", "IT", "Java", "Improved API", "Delivered");

        ErvaringDto result = service.saveSubmittedExperience(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBedrijf()).isEqualTo("Updated");
        assertThat(repository.savedEntities()).hasSize(1);
    }

    @Test
    void savesSubmittedExperiencesListWithExistingAndNewItems() {
        repository.put(TestData.experience(1L));
        ErvaringDto existing = TestData.experienceDto(1L);
        ErvaringDto created = new ErvaringDto(null, "New", "2025", "Lead", "IT", "Kotlin", "Led", "Delivered");

        List<ErvaringDto> result = service.saveSubmittedExperiencesList(List.of(existing, created));

        assertThat(result).extracting(ErvaringDto::getId).containsExactly(1L, 2L);
        assertThat(repository.savedEntities()).hasSize(2);
    }

    @Test
    void deletesExistingExperience() {
        repository.put(TestData.experience(1L));

        service.deleteExperience(TestData.experienceDto(1L));

        assertThat(repository.deletedIds()).containsExactly(1L);
        assertThat(repository.find(1L)).isEmpty();
    }

    @Test
    void deleteThrowsWhenExperienceDoesNotExist() {
        ErvaringDto missing = TestData.experienceDto(1L);

        assertThrows(EntityNotFoundException.class, () -> service.deleteExperience(missing));
    }
}
