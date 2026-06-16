package nl.codeclan.cvwiz;

import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.model.SkillMatrix;
import nl.codeclan.cvwiz.repository.SkillMatrixRepository;
import nl.codeclan.cvwiz.service.SkillMatrixService;
import nl.codeclan.cvwiz.support.RepositoryDoubles;
import nl.codeclan.cvwiz.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkillMatrixServiceTest {

    private RepositoryDoubles.TestRepository<SkillMatrixRepository, SkillMatrix, Long> repository;
    private SkillMatrixService service;

    @BeforeEach
    void setUp() {
        repository = RepositoryDoubles.skillMatrices();
        service = new SkillMatrixService(repository.repository());
    }

    @Test
    void createsFirstSkillMatrix() {
        service.createFirstSkillMatrix();

        SkillMatrix saved = repository.find(1L).orElseThrow();
        assertThat(saved.getSkills()).containsKeys("Backend Technologies", "Frontend Technologies");
    }

    @Test
    void createsNewSkillMatrixFromBaseMatrix() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        repository.put(TestData.skillMatrix(2L, "Frontend", "React"));

        TechniekMatrixDto result = service.createNewSkillMatrixOfBaseMatrix();

        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.matrix()).containsKey("Backend");
    }

    @Test
    void createsNewSkillMatrixFromBaseMatrixWithEmptyCategory() throws Exception {
        Map<String, Map<String, Integer>> skills = new HashMap<>();
        skills.put("Backend", null);
        repository.put(new SkillMatrix(1L, skills));

        TechniekMatrixDto result = service.createNewSkillMatrixOfBaseMatrix();

        assertThat(result.matrix()).containsKey("Backend");
        assertThat(result.matrix().get("Backend")).isEmpty();
    }

    @Test
    void savesSubmittedNewSkillMatrixWithGeneratedId() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        TechniekMatrixDto submitted = new TechniekMatrixDto(null, TestData.skillMap("Testing", "Playwright", 4));

        TechniekMatrixDto result = service.saveSubmittedSkillMatrix(submitted);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.matrix()).containsKey("Testing");
        assertThat(result.matrix()).containsKey("Backend");
        assertThat(repository.find(2L)).isPresent();
    }

    @Test
    void savesSubmittedNewSkillMatrixWithNextFreeIdWhenCountBasedIdExists() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        repository.put(TestData.skillMatrix(3L, "Frontend", "React"));
        TechniekMatrixDto submitted = new TechniekMatrixDto(null, TestData.skillMap("Testing", "Playwright", 4));

        TechniekMatrixDto result = service.saveSubmittedSkillMatrix(submitted);

        assertThat(result.id()).isEqualTo(4L);
        assertThat(repository.find(4L)).isPresent();
    }

    @Test
    void savesSubmittedNewSkillMatrixWithMissingBaseKeysAdded() throws Exception {
        repository.put(baseMatrixWithBackendAndFrontend());
        TechniekMatrixDto submitted = new TechniekMatrixDto(null, TestData.skillMap("Backend", "Java", 4));

        TechniekMatrixDto result = service.saveSubmittedSkillMatrix(submitted);

        assertThat(result.matrix().get("Backend")).containsEntry("Java", 4);
        assertThat(result.matrix().get("Backend")).containsEntry("Spring", 0);
        assertThat(result.matrix().get("Frontend")).containsEntry("React", 0);
    }

    @Test
    void savesSubmittedExistingSkillMatrixAsUpdate() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        TechniekMatrixDto submitted = TestData.matrixDto(1L, "Testing", "Playwright");

        TechniekMatrixDto result = service.saveSubmittedSkillMatrix(submitted);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.matrix()).containsKey("Testing");
        assertThat(result.matrix()).containsKey("Backend");
        assertThat(repository.savedEntities()).hasSize(1);
    }

    @Test
    void createNewSkillMatrixThrowsWhenBaseMatrixIsMissing() {
        assertThrows(FileNotFoundException.class, () -> service.createNewSkillMatrixOfBaseMatrix());
    }

    @Test
    void getsUpdatesAndDeletesExistingSkillMatrix() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        repository.put(TestData.skillMatrix(4L, "Backend", "Java"));
        TechniekMatrixDto update = TestData.matrixDto(4L, "Cloud", "AWS");

        TechniekMatrixDto found = service.getSkillMatrix(4L);
        TechniekMatrixDto updated = service.updateSkillMatrix(update);
        service.deleteSkillMatrix(4L);

        assertThat(found.id()).isEqualTo(4L);
        assertThat(updated.matrix()).containsKey("Cloud");
        assertThat(repository.deletedIds()).containsExactly(4L);
    }

    @Test
    void updatesSkillMatrixWithMissingBaseKeysAdded() throws Exception {
        repository.put(baseMatrixWithBackendAndFrontend());
        repository.put(TestData.skillMatrix(2L, "Backend", "Java"));
        TechniekMatrixDto submitted = new TechniekMatrixDto(2L, TestData.skillMap("Backend", "Java", 5));

        TechniekMatrixDto result = service.updateSkillMatrix(submitted);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.matrix().get("Backend")).containsEntry("Java", 5);
        assertThat(result.matrix().get("Backend")).containsEntry("Spring", 0);
        assertThat(result.matrix().get("Frontend")).containsEntry("React", 0);
    }

    @Test
    void missingSkillMatrixOperationsThrow() {
        assertThrows(FileNotFoundException.class, () -> service.getSkillMatrix(44L));
        assertThrows(FileNotFoundException.class, () -> service.updateSkillMatrix(null));
        assertThrows(FileNotFoundException.class, () -> service.updateSkillMatrix(new TechniekMatrixDto(null, TestData.skillMap("Backend", "Java", 1))));
        assertThrows(FileNotFoundException.class, () -> service.updateSkillMatrix(TestData.matrixDto(44L, "Backend", "Java")));
        assertThrows(FileNotFoundException.class, () -> service.deleteSkillMatrix(44L));
    }

    @Test
    void readsCategoriesAndToolsFromBaseMatrix() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));

        String categories = service.getAllCategoriesOfBaseSkillMatrix();
        String tools = service.getAllToolsOfBaseSkillMatrixCategory("Backend");

        assertThat(categories).contains("Backend");
        assertThat(tools).contains("Java");
    }

    @Test
    void categoryAndToolLookupsThrowForMissingData() {
        assertThrows(FileNotFoundException.class, () -> service.getAllCategoriesOfBaseSkillMatrix());
        assertThrows(FileNotFoundException.class, () -> service.getAllToolsOfBaseSkillMatrixCategory("Backend"));

        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));

        assertThrows(FileNotFoundException.class, () -> service.getAllToolsOfBaseSkillMatrixCategory("Frontend"));
    }

    @Test
    void addsNewCategoryToEveryMatrix() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        repository.put(TestData.skillMatrix(2L, "Backend", "Java"));

        String result = service.addNewCategoryToMapCategories("Frontend", "React");

        assertThat(result).contains("Frontend");
        assertThat(repository.find(1L).orElseThrow().getSkills()).containsKey("Frontend");
        assertThat(repository.find(2L).orElseThrow().getSkills()).containsKey("Frontend");
    }

    @Test
    void addsNewCategoryToMatrixWithMissingSkillsMap() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        repository.put(new SkillMatrix(2L, null));

        service.addNewCategoryToMapCategories("Frontend", "React");

        assertThat(repository.find(2L).orElseThrow().getSkills().get("Frontend")).containsEntry("React", 0);
    }

    @Test
    void addNewCategoryReturnsMessageWhenCategoryAlreadyExists() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));

        String result = service.addNewCategoryToMapCategories("Backend", "Spring");

        assertThat(result).contains("bestaat al");
    }

    @Test
    void addsNewToolToEveryMatrix() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        repository.put(TestData.skillMatrix(2L, "Backend", "Java"));

        String result = service.addNewToolToMapCategories("Backend", "Spring");

        assertThat(result).contains("Spring");
        assertThat(repository.find(1L).orElseThrow().getSkills().get("Backend")).containsKey("Spring");
        assertThat(repository.find(2L).orElseThrow().getSkills().get("Backend")).containsKey("Spring");
    }

    @Test
    void addsNewToolToMatrixWithMissingCategory() throws Exception {
        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        repository.put(new SkillMatrix(2L, new HashMap<>()));

        service.addNewToolToMapCategories("Backend", "Spring");

        assertThat(repository.find(2L).orElseThrow().getSkills().get("Backend")).containsEntry("Spring", 0);
    }

    @Test
    void addNewToolThrowsForMissingBaseMatrixExistingToolAndMissingCategory() {
        assertThrows(FileNotFoundException.class, () -> service.addNewToolToMapCategories("Backend", "Spring"));

        repository.put(TestData.skillMatrix(1L, "Backend", "Java"));

        assertThrows(FileNotFoundException.class, () -> service.addNewToolToMapCategories("Backend", "Java"));
        assertThrows(FileNotFoundException.class, () -> service.addNewToolToMapCategories("Frontend", "React"));
    }

    @Test
    void categoryChecksHandleMissingBaseCategoryAndTool() {
        assertThat(service.checkIfCategoryExistsInBaseMatrix("Backend")).isFalse();
        assertThat(service.checkIfCategoryContainsToolInBaseMatrix("Backend", "Java")).isFalse();

        repository.put(new SkillMatrix(1L, Map.of("Backend", Map.of("Java", 1))));

        assertThat(service.checkIfCategoryExistsInBaseMatrix("Backend")).isTrue();
        assertThat(service.checkIfCategoryContainsToolInBaseMatrix("Backend", "Java")).isTrue();
        assertThat(service.checkIfCategoryContainsToolInBaseMatrix("Frontend", "React")).isFalse();
    }

    private SkillMatrix baseMatrixWithBackendAndFrontend() {
        return new SkillMatrix(1L, Map.of(
                "Backend", Map.of("Java", 0, "Spring", 0),
                "Frontend", Map.of("React", 0)
        ));
    }
}
