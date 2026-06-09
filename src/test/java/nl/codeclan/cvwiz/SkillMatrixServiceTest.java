package nl.codeclan.cvwiz;


import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.mapper.SkillMatrixMapper;
import nl.codeclan.cvwiz.model.SkillMatrix;
import nl.codeclan.cvwiz.repository.SkillMatrixRepository;
import nl.codeclan.cvwiz.service.SkillMatrixService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SkillMatrixServiceTest {

    @InjectMocks
    private SkillMatrixService service;

    @Mock
    private SkillMatrixRepository repo;
    @Mock
    private SkillMatrix skim1;
    @Mock
    private SkillMatrix skim2;
    @Mock
    private SkillMatrix skim3;
    @Mock
    private TechniekMatrixDto dto;
    @Mock
    private TechniekMatrixDto dto1;
    @Mock
    private TechniekMatrixDto dto2;
    @Mock
    private SkillMatrix skim;

    @BeforeEach
    public void init() {
        Map<String, Map<String, Integer>> categories = new HashMap<>();
        Map<String, Integer> devMeth = new HashMap<>();
        devMeth.put("Scrum", 0);
        devMeth.put("Agile", 0);
        devMeth.put("Scaled Agile Framework (SAFe)", 0);
        devMeth.put("Behavior Driven Development (BDD)", 0);
        devMeth.put("DevOps", 0);
        devMeth.put("WCAG", 0);
        devMeth.put("Test Driven Development (TDD)", 0);
        categories.put("Methodology & Practices", devMeth);

        // Programming Languages
        Map<String, Integer> progLang = new HashMap<>();
        progLang.put("TypeScript", 0);
        progLang.put("Java", 0);
        progLang.put("JavaScript", 0);
        progLang.put("C#", 0);
        progLang.put("C", 0);
        progLang.put("C++", 0);
        progLang.put("Python", 0);
        progLang.put("Kotlin", 0);
        progLang.put("SQL", 0);
        progLang.put("PLSQL", 0);
        categories.put("Programming languages", progLang);

        // Frontend Technologies
        Map<String, Integer> fronTech = new HashMap<>();
        fronTech.put("RxJS", 0);
        fronTech.put("Flutter", 0);
        fronTech.put("Tailwind", 0);
        fronTech.put("Bootstrap", 0);
        fronTech.put("Storybook", 0);
        fronTech.put("Vite", 0);
        fronTech.put("NPM", 0);
        fronTech.put("ngBootstrap", 0);
        categories.put("Frontend Technologies", fronTech);

        skim1 = new SkillMatrix(1L, categories);
        skim2 = new SkillMatrix(2L, categories);
        skim3 = new SkillMatrix(3L, categories);
        dto = new TechniekMatrixDto(2L, categories);
        dto1 = new TechniekMatrixDto(3L, categories);
        Map<String,Map<String, Integer>> empty = new HashMap<>();
        skim = new SkillMatrix(1L, empty);
    }

    @Test
    public void createFirstSkillMatrix() {
        service.createFirstSkillMatrix();
        verify(repo, times(1)).save(any());
    }

    @Test
    public void createNewSkillMatrixOfBaseMatrixTest() throws FileNotFoundException {
        when(repo.findById(1L)).thenReturn(Optional.ofNullable(skim1));
        MockedStatic<SkillMatrixMapper> map = mockStatic(SkillMatrixMapper.class);
        map.when(() -> SkillMatrixMapper.mapSkillMatrixToDto(any())).thenReturn(dto);

        dto2 = service.createNewSkillMatrixOfBaseMatrix();
        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(any());
        assertThat(dto2.id()).isEqualTo(2L);
        map.close();
    }

    @Test
    public void createNewSkillMatrixOfBaseMatrixThrowsFileNotFoundExceptionTest()  {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrowsExactly(FileNotFoundException.class, () -> service.createNewSkillMatrixOfBaseMatrix());
    }

    @Test
    public void deleteSkillMatrixTest() throws FileNotFoundException {
        when(repo.findById(1L)).thenReturn(Optional.ofNullable(skim1));
        service.deleteSkillMatrix(1L);
        verify(repo, times(1)).delete(skim1);
    }

    @Test
    public void deleteSkillMatrixThrowsFileNotFoundExceptionTest()  {
        when(repo.findById(1L)).thenReturn(Optional.empty());
        assertThrowsExactly(FileNotFoundException.class, () -> service.deleteSkillMatrix(1L));
    }

    @Test
    public void updateSkillMatrixTest() throws FileNotFoundException {
        when(repo.findById(any())).thenReturn(Optional.ofNullable(skim1));
        MockedStatic<SkillMatrixMapper> map = mockStatic(SkillMatrixMapper.class);
        map.when(() -> SkillMatrixMapper.mapSkillMatrixToDto(any())).thenReturn(dto1);

        dto2 = service.updateSkillMatrix(SkillMatrixMapper.mapSkillMatrixToDto(skim3));
        assertThat(dto2.id()).isEqualTo(3L);
        assertThat(dto.matrix()).isEqualTo(skim3.getSkills());
        map.close();
    }

    @Test
    public void updateSkillMatrixThrowsFileNotFoundExceptionTest() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrowsExactly(FileNotFoundException.class, () -> service.updateSkillMatrix(dto));
    }

    @Test
    public void getSkillMatrixTest() throws FileNotFoundException {
        when(repo.findById(2L)).thenReturn(Optional.ofNullable(skim2));
        MockedStatic<SkillMatrixMapper> map = mockStatic(SkillMatrixMapper.class);
        map.when(() -> SkillMatrixMapper.mapSkillMatrixToDto(any())).thenReturn(dto);

        dto2 = service.getSkillMatrix(2L);
        assertThat(dto2.id()).isEqualTo(2L);
        assertThat(dto.matrix()).isEqualTo(skim2.getSkills());
        map.close();
    }

    @Test
    public void getSkillMatrixThrowsFileNotFoundExceptionTest() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrowsExactly(FileNotFoundException.class, () -> service.getSkillMatrix(2L));
    }

    @Test
    public void getAllCategoriesOfBaseSkillMatrixTest() throws FileNotFoundException {
        when(repo.findById(1L)).thenReturn(Optional.ofNullable(skim1));

        String s = service.getAllCategoriesOfBaseSkillMatrix();
        assertThat(s).isEqualTo("Alle catagorieën in de matrix zijn: [Frontend Technologies, Methodology & Practices, Programming languages]");
    }

    @Test
    public void getAllCategoriesOfBaseSkillMatrixThrowsFileNotFoundExceptionTest() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrowsExactly(FileNotFoundException.class, () -> service.getAllCategoriesOfBaseSkillMatrix());
    }

    @Test
    public void getAllToolsOfBaseSkillMatrixCategoryTest() throws FileNotFoundException {
        when(repo.findById(1L)).thenReturn(Optional.ofNullable(skim1));

        String s = service.getAllToolsOfBaseSkillMatrixCategory("Programming languages");
        assertThat(s).isEqualTo("Alle skills in de categorie \"Programming languages\" zijn: [TypeScript, C#, Java, C++, C, JavaScript, PLSQL, Python, Kotlin, SQL]");
    }

    @Test
    public void getAllToolsOfBaseSkillMatrixThrowsFileNotFoundExceptionWhenNoBaseMatrixIsFoundTest() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrowsExactly(FileNotFoundException.class, () -> service.getAllToolsOfBaseSkillMatrixCategory("Programming languages"));
    }

    @Test
    public void getAllToolsOfBaseSkillMatrixThrowsFileNotFoundExceptionWhenNoCategoryIsFoundTest() {
        when(repo.findById(1L)).thenReturn(Optional.of(skim1));
        assertThrowsExactly(FileNotFoundException.class, () -> service.getAllToolsOfBaseSkillMatrixCategory("Skipping Stones"));
    }

    @Test
    public void addNewCategoryToMapCategoriesTest() throws FileNotFoundException {
        when(repo.findById(1L)).thenReturn(Optional.ofNullable(skim1));
        when(repo.findAll()).thenReturn(Arrays.asList(skim1, skim2, skim3));

        String s = service.addNewCategoryToMapCategories("Skipping Stones", "Skipping Stones");
        assertThat(skim1.getSkills().containsKey("Skipping Stones")).isTrue();
        assertThat(skim2.getSkills().containsKey("Skipping Stones")).isTrue();
        assertThat(skim3.getSkills().containsKey("Skipping Stones")).isTrue();
        assertThat(s).isEqualTo("De category: Skipping Stones met de techniek: Skipping Stones is succesvol toegevoegd aan de matrix categorieën en alle matrices zijn bijgewerkt! De matrix bestaat nu uit de volgende categorieën: Alle catagorieën in de matrix zijn: [Frontend Technologies, Methodology & Practices, Skipping Stones, Programming languages]");
    }

    @Test
    public void addNewCategoryToMapCategoriesWhenCategoryAlreadyExistInMatrixTest() throws FileNotFoundException {
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.getReferenceById(1L)).thenReturn(skim1);

        String s = service.addNewCategoryToMapCategories("Frontend Technologies",  "Skipping Stones");
        assertThat(s).isEqualTo("De category: Frontend Technologies bestaat al in de basis matrix!");
    }

    @Test
    public void addNewToolToBaseSkillMatrixCategoryTest() throws FileNotFoundException {
        when(repo.findById(1L)).thenReturn(Optional.ofNullable(skim1));
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.getReferenceById(1L)).thenReturn(skim1);
        when(repo.findAll()).thenReturn(Arrays.asList(skim1, skim2, skim3));

        String s = service.addNewToolToMapCategories("Frontend Technologies", "Skipping Stones");
        assertThat(skim1.getSkills().get("Frontend Technologies").containsKey("Skipping Stones")).isTrue();
        assertThat(skim2.getSkills().get("Frontend Technologies").containsKey("Skipping Stones")).isTrue();
        assertThat(skim3.getSkills().get("Frontend Technologies").containsKey("Skipping Stones")).isTrue();
        assertThat(s).isEqualTo("De category Frontend Technologies is succesvol aangevuld met Skipping Stones. En bestaat nu uit de volgende skills: Alle skills in de categorie \"Frontend Technologies\" zijn: [Storybook, Skipping Stones, RxJS, Bootstrap, Flutter, NPM, Vite, ngBootstrap, Tailwind]");
    }

    @Test
    public void addNewToolToBaseSkillMatrixCategoryThrowsFileNotFoundExceptionIfCategorDoesntExistTest() {
        when(repo.findById(1L)).thenReturn(Optional.ofNullable(skim1));

        assertThrowsExactly(FileNotFoundException.class, () -> service.addNewToolToMapCategories("Skipping Stones", "Skipping Stones"));
    }

    @Test
    public void addNewToolToBaseSkillMatrixCategoryThrowsFileNotFoundExceptionIfToolAlreadyExistInCategoryTest() {
        when(repo.findById(1L)).thenReturn(Optional.ofNullable(skim1));
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.getReferenceById(1L)).thenReturn(skim1);

        assertThrowsExactly(FileNotFoundException.class, () -> service.addNewToolToMapCategories("Frontend Technologies", "Storybook"));
    }

    @Test
    public void addNewToolToBaseSkillMatrixCategoryThrowsFileNotFoundExceptionIfThereIsNoBaseMatrixTest() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrowsExactly(FileNotFoundException.class, () -> service.addNewToolToMapCategories("Frontend Technologies", "React"));
    }

    //  By definition already tested in AddNewCategoryToMapCategoriesTest, but to define quicker when there is a problem created by this check, extra tests is are added here
    @Test
    public void checkIfCategoryExistsInBaseMatrixTest() {
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.getReferenceById(1L)).thenReturn(skim1);

        assertThat(service.checkIfCategoryExistsInBaseMatrix("Frontend Technologies")).isEqualTo(true);
    }

    @Test
    public void checkIfCategoryExistsReturnsFalseIfCategoryDoesntExistInBaseMatrixTest() {
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.getReferenceById(1L)).thenReturn(skim1);

        assertThat(service.checkIfCategoryExistsInBaseMatrix("Skipping Stones")).isEqualTo(false);
    }

    @Test
    public void checkIfCategoryExistsReturnsFalseIfThereIsNoBaseMatrixTest() {
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.getReferenceById(1L)).thenReturn(skim1);

        assertThat(service.checkIfCategoryExistsInBaseMatrix("Skipping Stones")).isEqualTo(false);
    }

    //  By definition already tested in AddNewToolToMapCategories, but to define quicker when there is a problem created by this check, extra tests is are added here
    @Test
    public void checkIfCategoryContainsToolsInBaseMatrixWithExistingCategoryAndToolTest() {
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.getReferenceById(1L)).thenReturn(skim1);

        assertThat(service.checkIfCategoryContainsToolInBaseMatrix("Frontend Technologies", "Storybook")).isEqualTo(true);
    }

    @Test
    public void checkIfCategoryContainsToolsInBaseMatrixWithExistingCategoryAndNotExistingToolTest() {
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.getReferenceById(1L)).thenReturn(skim1);

        assertThat(service.checkIfCategoryContainsToolInBaseMatrix("Frontend Technologies", "Skipping Stones")).isEqualTo(false);
    }

    @Test
    public void checkIfCategoryContainsToolsInBaseMatrixWithNotExistingCategoryAndToolTest() {
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.getReferenceById(1L)).thenReturn(skim1);

        assertThat(service.checkIfCategoryContainsToolInBaseMatrix("Skipping Stones", "React")).isEqualTo(false);
    }

    @Test
    public void checkIfCategoryContainsToolsInBaseMatrixWithOutExistingBaseMatrixTest() {
        when(repo.existsById(1L)).thenReturn(false);

        assertThat(service.checkIfCategoryContainsToolInBaseMatrix("Frontend Technologies", "React")).isEqualTo(false);
    }
}
