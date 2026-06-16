package nl.codeclan.cvwiz.service;


import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.mapper.SkillMatrixMapper;
import nl.codeclan.cvwiz.model.SkillMatrix;
import nl.codeclan.cvwiz.repository.SkillMatrixRepository;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SkillMatrixService {

    private final SkillMatrixRepository repo;

    public SkillMatrixService(SkillMatrixRepository repo) {
        this.repo = repo;
    }

    public void createFirstSkillMatrix() {
        Map<String, Map<String, Integer>> categories = new HashMap<>();
        Map<String, Integer> front = new HashMap<>();
        Map<String, Integer> back = new HashMap<>();
        back.put("Java", 0);
        front.put("React", 0);
        categories.put("Backend Technologies", front);
        categories.put("Frontend Technologies", back);
        repo.save(new SkillMatrix(1L, categories));
    }

    public TechniekMatrixDto createNewSkillMatrixOfBaseMatrix() throws FileNotFoundException {
        SkillMatrix baseMatrix = getBaseSkillMatrix();
        SkillMatrix newMatrix = new SkillMatrix();
        newMatrix.setSkills(copySkills(baseMatrix.getSkills()));
        newMatrix.setSkillMatrixId(nextSkillMatrixId());
        return SkillMatrixMapper.mapSkillMatrixToDto(repo.save(newMatrix));
    }

    public TechniekMatrixDto saveSubmittedSkillMatrix(TechniekMatrixDto dto) throws FileNotFoundException {
        if (dto == null) {
            return createNewSkillMatrixOfBaseMatrix();
        }
        TechniekMatrixDto completeDto = addMissingBaseKeys(dto);
        if (completeDto.id() == null) {
            return createNewSkillMatrix(completeDto);
        }
        if (repo.existsById(completeDto.id())) {
            return updateSkillMatrix(completeDto);
        }
        return SkillMatrixMapper.mapSkillMatrixToDto(repo.save(SkillMatrixMapper.mapDtoToSkillMatrix(completeDto)));
    }

    private TechniekMatrixDto createNewSkillMatrix(TechniekMatrixDto dto) {
        SkillMatrix matrix = SkillMatrixMapper.mapDtoToSkillMatrix(new TechniekMatrixDto(nextSkillMatrixId(), dto.matrix()));
        return SkillMatrixMapper.mapSkillMatrixToDto(repo.save(matrix));
    }

    public void deleteSkillMatrix(long id) throws FileNotFoundException {
        Optional<SkillMatrix> matrix = repo.findById(id);
        if (matrix.isPresent()) {
            repo.delete(matrix.get());
        } else {
            throw new FileNotFoundException("Geen matrix met dit id gevonden!");
        }
    }

    public TechniekMatrixDto updateSkillMatrix(TechniekMatrixDto dto) throws FileNotFoundException {
        if (dto == null || dto.id() == null) {
            throw new FileNotFoundException("Geen matrix met dit id gevonden!");
        }
        Optional<SkillMatrix> matrix = repo.findById(dto.id());
        if (matrix.isPresent()) {
            TechniekMatrixDto completeDto = addMissingBaseKeys(dto);
            return SkillMatrixMapper.mapSkillMatrixToDto(repo.save(SkillMatrixMapper.mapDtoToSkillMatrix(completeDto)));
        } else {
            throw new FileNotFoundException("Geen matrix met dit id gevonden!");
        }
    }

    public TechniekMatrixDto getSkillMatrix(long id) throws FileNotFoundException {
        Optional<SkillMatrix> matrix = repo.findById(id);
        if (matrix.isPresent()) {
            return SkillMatrixMapper.mapSkillMatrixToDto(matrix.get());
        } else {
            throw new FileNotFoundException("Geen skillmatrix met dit id gevonden!");
        }
    }

    public String getAllCategoriesOfBaseSkillMatrix() throws FileNotFoundException {
        Optional<SkillMatrix> matrix = repo.findById(1L);
        if (matrix.isPresent()) {
            SkillMatrix baseMatrix = matrix.get();
            return "Alle catagorieën in de matrix zijn: " + baseMatrix.getSkills().keySet();
        } else {
            throw new FileNotFoundException("Er zijn geen matrices in de database gevonden!");
        }
    }

    public String getAllToolsOfBaseSkillMatrixCategory(String category) throws FileNotFoundException {
        Optional<SkillMatrix> matrix = repo.findById(1L);
        if (matrix.isPresent()) {
            SkillMatrix baseMatrix = matrix.get();
            if (baseMatrix.getSkills().containsKey(category)) {
                return "Alle skills in de categorie \"" + category + "\" zijn: " + baseMatrix.getSkills().get(category).keySet();
            } else {
                throw new FileNotFoundException("Er geen category gevonden met de naam " + category + "!");
            }
        } else {
            throw new FileNotFoundException("Er zijn geen matrices in de database gevonden!");
        }

    }

    public String addNewCategoryToMapCategories(String category, String techniek) throws FileNotFoundException {
        if (!checkIfCategoryExistsInBaseMatrix(category)) {
//        to keep all the SkillMatrices up to date this method adds a new category to all existing matrices
            List<SkillMatrix> matrices = repo.findAll();
            for (SkillMatrix matrix : matrices) {
                Map<String, Map<String, Integer>> categories = copySkills(matrix.getSkills());
                categories.computeIfAbsent(category, key -> new HashMap<>()).putIfAbsent(techniek, 0);
                matrix.setSkills(categories);
            }
            repo.saveAll(matrices);
            return "De category: " + category + " met de techniek: " + techniek + " is succesvol toegevoegd aan de matrix categorieën en alle matrices zijn bijgewerkt! De matrix bestaat nu uit de volgende categorieën: " + getAllCategoriesOfBaseSkillMatrix();
        } else {
            return "De category: " + category + " bestaat al in de basis matrix!";
        }
    }

    public String addNewToolToMapCategories(String category, String tool) throws FileNotFoundException {
        SkillMatrix baseMatrix = getBaseSkillMatrix();
        Map<String, Map<String, Integer>> baseSkills = copySkills(baseMatrix.getSkills());
        if (!baseSkills.containsKey(category)) {
            throw new FileNotFoundException("Er geen category gevonden met de naam " + category + "!");
        }
        if (baseSkills.get(category).containsKey(tool)) {
            throw new FileNotFoundException("De category: " + category + " bevat al de tool: " + tool + "!");
        }

//      to keep all the SkillMatrices up to date this method adds the new skill to all matrices
        List<SkillMatrix> matrices = repo.findAll();
        for (SkillMatrix matrix : matrices) {
            Map<String, Map<String, Integer>> categories = copySkills(matrix.getSkills());
            categories.computeIfAbsent(category, key -> new HashMap<>()).putIfAbsent(tool, 0);
            matrix.setSkills(categories);
        }
        repo.saveAll(matrices);
        return "De category " + category + " is succesvol aangevuld met " + tool + ". En bestaat nu uit de volgende skills: " + getAllToolsOfBaseSkillMatrixCategory(category);
    }

    public boolean checkIfCategoryExistsInBaseMatrix(String category) {
        if (repo.existsById(1L)) {
            SkillMatrix matrix = repo.getReferenceById(1L);
            return matrix.getSkills() != null && matrix.getSkills().containsKey(category);
        } else {
            return false;
        }
    }

    public boolean checkIfCategoryContainsToolInBaseMatrix(String category, String tool) {
        if (repo.existsById(1L)) {
            SkillMatrix matrix = repo.getReferenceById(1L);
            if (matrix.getSkills() != null && matrix.getSkills().containsKey(category) && matrix.getSkills().get(category) != null) {
                return matrix.getSkills().get(category).containsKey(tool);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private Long nextSkillMatrixId() {
        long id = repo.count() + 1;
        while (repo.existsById(id)) {
            id++;
        }
        return id;
    }

    private SkillMatrix getBaseSkillMatrix() throws FileNotFoundException {
        return repo.findById(1L)
                .orElseThrow(() -> new FileNotFoundException("Er is geen basis matrix aanwezig in de database!"));
    }

    private TechniekMatrixDto addMissingBaseKeys(TechniekMatrixDto dto) throws FileNotFoundException {
        Map<String, Map<String, Integer>> completeSkills = addMissingBaseKeys(dto.matrix(), getBaseSkillMatrix().getSkills());
        return new TechniekMatrixDto(dto.id(), completeSkills);
    }

    private Map<String, Map<String, Integer>> addMissingBaseKeys(Map<String, Map<String, Integer>> submitted, Map<String, Map<String, Integer>> base) {
        Map<String, Map<String, Integer>> submittedSkills = copySkills(submitted);
        copySkills(base).forEach((category, baseTools) -> {
            Map<String, Integer> submittedTools = submittedSkills.computeIfAbsent(category, key -> new HashMap<>());
            baseTools.forEach(submittedTools::putIfAbsent);
        });
        return submittedSkills;
    }

    private Map<String, Map<String, Integer>> copySkills(Map<String, Map<String, Integer>> skills) {
        if (skills == null) {
            return new HashMap<>();
        }
        Map<String, Map<String, Integer>> copy = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : skills.entrySet()) {
            copy.put(entry.getKey(), entry.getValue() == null ? new HashMap<>() : new HashMap<>(entry.getValue()));
        }
        return copy;
    }
}
