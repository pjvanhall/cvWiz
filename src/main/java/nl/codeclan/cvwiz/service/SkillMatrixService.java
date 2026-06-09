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

    /// /    This method is made to initialize a first matrix to be used as base matrix. To unify all existing matrices
//    @PostConstruct todo delete commented out when runnig without data.sql
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
        Optional<SkillMatrix> optional = repo.findById(1L);
        if (optional.isPresent()) {
            SkillMatrix matrix = optional.get();
            SkillMatrix newMatrix = new SkillMatrix();
            newMatrix.setSkills(matrix.getSkills());
            newMatrix.setSkillMatrixId(repo.count() + 1);
            return SkillMatrixMapper.mapSkillMatrixToDto(repo.save(newMatrix));
        } else {
            throw new FileNotFoundException("Er is geen basis matrix aanwezig in de database!");
        }
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
        Optional<SkillMatrix> matrix = repo.findById(dto.id());
        if (matrix.isPresent()) {
            return SkillMatrixMapper.mapSkillMatrixToDto(repo.save(SkillMatrixMapper.mapDtoToSkillMatrix(dto)));
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
        Map<String, Integer> tool = new HashMap<>();
        tool.put(techniek, 0);
        if (!checkIfCategoryExistsInBaseMatrix(category)) {
//        to keep all the SkillMatrices up to date this method adds a new category to all existing matrices
            List<SkillMatrix> matrices = repo.findAll();
            for (SkillMatrix matrix : matrices) {
                Map<String, Map<String, Integer>> categories = matrix.getSkills();
                categories.put(category, tool);
            }
            repo.saveAll(matrices);
            return "De category: " + category + " met de techniek: " + techniek + " is succesvol toegevoegd aan de matrix categorieën en alle matrices zijn bijgewerkt! De matrix bestaat nu uit de volgende categorieën: " + getAllCategoriesOfBaseSkillMatrix();
        } else {
            return "De category: " + category + " bestaat al in de basis matrix!";
        }
    }

    public String addNewToolToMapCategories(String category, String tool) throws FileNotFoundException {
        Optional<SkillMatrix> optional = repo.findById(1L);
        if (optional.isPresent()) {
            SkillMatrix matrix = optional.get();
            Map<String, Map<String, Integer>> categories = matrix.getSkills();
            Map<String, Integer> tools = categories.get(category);
            if (!checkIfCategoryContainsToolInBaseMatrix(category, tool) && checkIfCategoryExistsInBaseMatrix(category)) {
                tools.put(tool, 0);
//            to keep all the SkillMatrices up to date this method adds the new skill to all matrices
                List<SkillMatrix> matrices = repo.findAll();
                for (SkillMatrix m : matrices) {
                    Map<String, Map<String, Integer>> cat = m.getSkills();
                    Map<String, Integer> toolList = cat.get(category);
                    toolList.put(tool, 0);
                }
                repo.saveAll(matrices);
                return "De category " + category + " is succesvol aangevuld met " + tool + ". En bestaat nu uit de volgende skills: " + getAllToolsOfBaseSkillMatrixCategory(category);
            } else if (checkIfCategoryExistsInBaseMatrix(category)) {
                throw new FileNotFoundException("Er geen category gevonden met de naam " + category + "!");
            } else {
                throw new FileNotFoundException("De category: " + category + " bevat al de tool: " + tool + "!");
            }
        } else {
            throw new FileNotFoundException("Er zijn geen matrices in de database gevonden!");
        }
    }

    public boolean checkIfCategoryExistsInBaseMatrix(String category) {
        if (repo.existsById(1L)) {
            SkillMatrix matrix = repo.getReferenceById(1L);
            return matrix.getSkills().containsKey(category);
        } else {
            return false;
        }
    }

    public boolean checkIfCategoryContainsToolInBaseMatrix(String category, String tool) {
        if (repo.existsById(1L)) {
            SkillMatrix matrix = repo.getReferenceById(1L);
            if (matrix.getSkills().containsKey(category)) {
                return matrix.getSkills().get(category).containsKey(tool);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
