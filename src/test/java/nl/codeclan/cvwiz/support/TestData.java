package nl.codeclan.cvwiz.support;

import nl.codeclan.cvwiz.dto.BeheerderDto;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.mapper.CVMapper;
import nl.codeclan.cvwiz.model.Authorisatie;
import nl.codeclan.cvwiz.model.Consultant;
import nl.codeclan.cvwiz.model.CustomUser;
import nl.codeclan.cvwiz.model.Cv;
import nl.codeclan.cvwiz.model.Experience;
import nl.codeclan.cvwiz.model.Manager;
import nl.codeclan.cvwiz.model.SkillMatrix;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TestData {

    public static final UUID MANAGER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID CONSULTANT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID OTHER_CONSULTANT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private TestData() {
    }

    public static Map<String, Map<String, Integer>> skillMap(String category, String tool, int score) {
        Map<String, Integer> tools = new LinkedHashMap<>();
        tools.put(tool, score);
        Map<String, Map<String, Integer>> categories = new LinkedHashMap<>();
        categories.put(category, tools);
        return categories;
    }

    public static Map<String, String> languageMap() {
        Map<String, String> languages = new LinkedHashMap<>();
        languages.put("Dutch", "Native");
        languages.put("English", "Professional");
        return languages;
    }

    public static SkillMatrix skillMatrix(long id, String category, String tool) {
        return new SkillMatrix(id, skillMap(category, tool, 0));
    }

    public static TechniekMatrixDto matrixDto(long id, String category, String tool) {
        return new TechniekMatrixDto(id, skillMap(category, tool, 1));
    }

    public static ErvaringDto experienceDto(long id) {
        return new ErvaringDto(id, "CodeClan", "2024", "Developer", "IT", "Java", "Built APIs", "Implemented features");
    }

    public static Experience experience(long id) {
        return new Experience(id, "CodeClan", "2024", "Developer", "IT", "Java", "Built APIs", "Implemented features");
    }

    public static CurriculumVitaeDto cvDto(long id) {
        return new CurriculumVitaeDto(
                id,
                "cv-" + id + ".pdf",
                new ArrayList<>(List.of("Java", "Spring")),
                languageMap(),
                "Profile " + id,
                "Education " + id,
                matrixDto(id, "Backend", "Java"),
                new ArrayList<>(List.of(experienceDto(id)))
        );
    }

    public static Cv cv(long id) {
        return CVMapper.mapCVDtoToCV(cvDto(id));
    }

    public static MedewerkerDto consultantDto(UUID id, CurriculumVitaeDto originalCv, List<CurriculumVitaeDto> cvs) {
        return new MedewerkerDto("Jane", id == null ? null : id.toString(), "Doe", "0612345678", "jane@example.com", originalCv, cvs);
    }

    public static Consultant consultant(UUID id, Cv originalCv, List<Cv> cvs) {
        return new Consultant(id, "Jane", "Doe", "0612345678", "jane@example.com", originalCv, cvs);
    }

    public static BeheerderDto managerDto(UUID id) {
        return new BeheerderDto("John", id == null ? null : id.toString(), "Manager", "0698765432", "john.manager@example.com");
    }

    public static Manager manager(UUID id) {
        return new Manager(id, "John", "Manager", "0698765432", "john.manager@example.com");
    }

    public static CustomUser user(String username, String role) {
        CustomUser user = new CustomUser(username, username + "@example.com", "encoded:password", true);
        user.addAuthorisatie(new Authorisatie(username, role));
        return user;
    }
}
