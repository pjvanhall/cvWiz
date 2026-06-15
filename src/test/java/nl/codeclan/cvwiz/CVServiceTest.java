package nl.codeclan.cvwiz;

import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.model.Cv;
import nl.codeclan.cvwiz.model.Experience;
import nl.codeclan.cvwiz.model.SkillMatrix;
import nl.codeclan.cvwiz.repository.CvRepository;
import nl.codeclan.cvwiz.repository.ExperienceRepository;
import nl.codeclan.cvwiz.repository.SkillMatrixRepository;
import nl.codeclan.cvwiz.service.CvService;
import nl.codeclan.cvwiz.service.ExperienceService;
import nl.codeclan.cvwiz.service.SkillMatrixService;
import nl.codeclan.cvwiz.support.RepositoryDoubles;
import nl.codeclan.cvwiz.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CVServiceTest {

    private RepositoryDoubles.TestRepository<CvRepository, Cv, Long> cvRepository;
    private RepositoryDoubles.TestRepository<ExperienceRepository, Experience, Long> experienceRepository;
    private RepositoryDoubles.TestRepository<SkillMatrixRepository, SkillMatrix, Long> skillMatrixRepository;
    private CvService service;

    @BeforeEach
    void setUp() {
        cvRepository = RepositoryDoubles.cvs();
        experienceRepository = RepositoryDoubles.experiences();
        skillMatrixRepository = RepositoryDoubles.skillMatrices();
        ExperienceService experienceService = new ExperienceService(experienceRepository.repository());
        SkillMatrixService skillMatrixService = new SkillMatrixService(skillMatrixRepository.repository());
        service = new CvService(cvRepository.repository(), experienceService, skillMatrixService);
    }

    @Test
    void createsCvForNewConsultantFromNullDto() throws Exception {
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));

        CurriculumVitaeDto result = service.createCVForNewConsultant(null);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getErvaring()).hasSize(1);
        assertThat(result.getLanguages()).isEmpty();
        assertThat(result.getMatrix().id()).isEqualTo(2L);
        assertThat(cvRepository.savedEntities()).hasSize(1);
        assertThat(cvRepository.savedEntities().getFirst().getLanguages()).isEmpty();
    }

    @Test
    void createsCvForNewConsultantFromExistingDto() throws Exception {
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        CurriculumVitaeDto dto = TestData.cvDto(99L);

        CurriculumVitaeDto result = service.createCVForNewConsultant(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBestandsNaam()).isEqualTo("cv-99.pdf");
    }

    @Test
    void createsNewCvAndPersistsExperiences() throws Exception {
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        CurriculumVitaeDto dto = TestData.cvDto(99L);

        CurriculumVitaeDto result = service.createNewCv(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLanguages()).containsEntry("Dutch", "Native").containsEntry("English", "Professional");
        assertThat(experienceRepository.savedEntities()).hasSize(1);
        assertThat(cvRepository.find(1L)).hasValueSatisfying(cv -> assertThat(cv.getLanguages()).containsEntry("Dutch", "Native").containsEntry("English", "Professional"));
    }

    @Test
    void createsNewCvWithNextFreeIdWhenCountBasedIdExists() throws Exception {
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        cvRepository.put(TestData.cv(2L));
        CurriculumVitaeDto dto = TestData.cvDto(99L);

        CurriculumVitaeDto result = service.createNewCv(dto);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(cvRepository.find(3L)).isPresent();
    }

    @Test
    void createsNewCvWithSubmittedNewMatrix() throws Exception {
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        CurriculumVitaeDto dto = TestData.cvDto(99L);
        dto.setMatrix(new TechniekMatrixDto(null, TestData.skillMap("Testing", "Playwright", 4)));

        CurriculumVitaeDto result = service.createNewCv(dto);

        assertThat(result.getMatrix().id()).isEqualTo(2L);
        assertThat(result.getMatrix().matrix()).containsKey("Testing");
        assertThat(result.getMatrix().matrix()).containsKey("Backend");
        assertThat(skillMatrixRepository.find(2L)).isPresent();
    }

    @Test
    void createsNewCvWithDefaultMatrixAndEmptyExperiencesWhenMissing() throws Exception {
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        CurriculumVitaeDto dto = TestData.cvDto(99L);
        dto.setMatrix(null);
        dto.setErvaring(null);

        CurriculumVitaeDto result = service.createNewCv(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMatrix()).isNotNull();
        assertThat(result.getMatrix().id()).isEqualTo(2L);
        assertThat(result.getErvaring()).isEmpty();
        assertThat(cvRepository.find(1L)).isPresent();
    }

    @Test
    void getsExistingCvAndThrowsForMissingCv() throws Exception {
        cvRepository.put(TestData.cv(1L));

        CurriculumVitaeDto found = service.getCvById(1L);

        assertThat(found.getId()).isEqualTo(1L);
        assertThrows(FileNotFoundException.class, () -> service.getCvById(2L));
    }

    @Test
    void updatesCvAndPropagatesMatrixChanges() throws Exception {
        cvRepository.put(TestData.cv(1L));
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        CurriculumVitaeDto updated = TestData.cvDto(1L);
        updated.setMatrix(TestData.matrixDto(1L, "Frontend", "React"));

        CurriculumVitaeDto result = service.updateCV(updated);

        assertThat(result.getMatrix().matrix()).containsKey("Frontend");
        assertThat(skillMatrixRepository.savedEntities()).hasSize(1);
    }

    @Test
    void updatesCvAndPropagatesExperienceChanges() throws Exception {
        cvRepository.put(TestData.cv(1L));
        CurriculumVitaeDto updated = TestData.cvDto(1L);
        updated.setErvaring(new ArrayList<>(List.of(new ErvaringDto(1L, "Other", "2025", "Lead", "IT", "Kotlin", "Led", "Delivered"))));

        CurriculumVitaeDto result = service.updateCV(updated);

        assertThat(result.getErvaring()).extracting(ErvaringDto::getBedrijf).containsExactly("Other");
        assertThat(experienceRepository.savedEntities()).hasSize(1);
    }

    @Test
    void updatesCvLanguages() throws Exception {
        cvRepository.put(TestData.cv(1L));
        CurriculumVitaeDto updated = TestData.cvDto(1L);
        Map<String, String> languages = new LinkedHashMap<>();
        languages.put("Dutch", "Native");
        languages.put("English", "Professional");
        languages.put("German", "Intermediate");
        updated.setLanguages(languages);

        CurriculumVitaeDto result = service.updateCV(updated);

        assertThat(result.getLanguages()).containsEntry("Dutch", "Native").containsEntry("English", "Professional").containsEntry("German", "Intermediate");
        assertThat(cvRepository.savedEntities().getFirst().getLanguages()).containsEntry("Dutch", "Native").containsEntry("English", "Professional").containsEntry("German", "Intermediate");
    }

    @Test
    void updatesCvAndCreatesSubmittedNewExperiences() throws Exception {
        cvRepository.put(TestData.cv(1L));
        experienceRepository.put(TestData.experience(1L));
        CurriculumVitaeDto updated = TestData.cvDto(1L);
        updated.setErvaring(new ArrayList<>(List.of(
                new ErvaringDto(1L, "Existing", "2024", "Developer", "IT", "Java", "Built APIs", "Implemented features"),
                new ErvaringDto(null, "New", "2025", "Lead", "IT", "Kotlin", "Led", "Delivered")
        )));

        CurriculumVitaeDto result = service.updateCV(updated);

        assertThat(result.getErvaring()).extracting(ErvaringDto::getId).containsExactly(1L, 2L);
        assertThat(result.getErvaring()).extracting(ErvaringDto::getBedrijf).containsExactly("Existing", "New");
        assertThat(experienceRepository.savedEntities()).hasSize(2);
    }

    @Test
    void updatesCvAndPropagatesMatrixAndExperienceChangesTogether() throws Exception {
        cvRepository.put(TestData.cv(1L));
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        CurriculumVitaeDto updated = TestData.cvDto(1L);
        updated.setMatrix(TestData.matrixDto(1L, "Frontend", "React"));
        updated.setErvaring(new ArrayList<>(List.of(new ErvaringDto(1L, "Other", "2025", "Lead", "IT", "Kotlin", "Led", "Delivered"))));

        CurriculumVitaeDto result = service.updateCV(updated);

        assertThat(result.getMatrix().matrix()).containsKey("Frontend");
        assertThat(result.getErvaring()).extracting(ErvaringDto::getBedrijf).containsExactly("Other");
        assertThat(skillMatrixRepository.savedEntities()).hasSize(1);
        assertThat(experienceRepository.savedEntities()).hasSize(1);
    }

    @Test
    void updateCvThrowsWhenMissing() {
        assertThrows(FileNotFoundException.class, () -> service.updateCV(TestData.cvDto(1L)));
    }

    @Test
    void deletesCvAndItsMatrixAndExperiences() throws Exception {
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        experienceRepository.put(TestData.experience(1L));
        cvRepository.put(TestData.cv(1L));

        service.deleteCvById(1L);

        assertThat(skillMatrixRepository.deletedIds()).containsExactly(1L);
        assertThat(experienceRepository.deletedIds()).containsExactly(1L);
        assertThat(cvRepository.deletedIds()).containsExactly(1L);
    }

    @Test
    void deleteCvThrowsWhenMissing() {
        assertThrows(FileNotFoundException.class, () -> service.deleteCvById(1L));
    }

    @Test
    void updatesOriginalCvOnlyWhenIdsMatchAndCvExists() throws Exception {
        cvRepository.put(TestData.cv(1L));
        CurriculumVitaeDto original = TestData.cvDto(1L);
        CurriculumVitaeDto updated = TestData.cvDto(1L);
        updated.setProfiel("Updated profile");

        CurriculumVitaeDto result = service.updateOriginalCV(TestData.consultantDto(TestData.CONSULTANT_ID, original, List.of()), updated);

        assertThat(result.getProfiel()).isEqualTo("Updated profile");
    }

    @Test
    void updateOriginalCvThrowsForMismatchedOrMissingIds() {
        CurriculumVitaeDto original = TestData.cvDto(1L);

        assertThrows(FileNotFoundException.class, () -> service.updateOriginalCV(TestData.consultantDto(TestData.CONSULTANT_ID, original, List.of()), TestData.cvDto(2L)));
        assertThrows(FileNotFoundException.class, () -> service.updateOriginalCV(TestData.consultantDto(TestData.CONSULTANT_ID, original, List.of()), TestData.cvDto(1L)));
    }

    @Test
    void updateOriginalCvThrowsWhenOriginalCvContextIsMissing() {
        assertThrows(FileNotFoundException.class, () -> service.updateOriginalCV(null, TestData.cvDto(1L)));
        assertThrows(FileNotFoundException.class, () -> service.updateOriginalCV(TestData.consultantDto(TestData.CONSULTANT_ID, null, List.of()), TestData.cvDto(1L)));
        assertThrows(FileNotFoundException.class, () -> service.updateOriginalCV(TestData.consultantDto(TestData.CONSULTANT_ID, TestData.cvDto(1L), List.of()), null));
    }

    @Test
    void reportsWhetherCvExists() {
        cvRepository.put(TestData.cv(1L));

        assertThat(service.cvExist(1L)).isTrue();
        assertThat(service.cvExist(2L)).isFalse();
        assertThat(service.cvExist(null)).isFalse();
    }
}
