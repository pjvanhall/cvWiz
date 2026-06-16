package nl.codeclan.cvwiz;

import nl.codeclan.cvwiz.dto.MedewerkerOnboardingResponseDto;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.model.Consultant;
import nl.codeclan.cvwiz.model.CustomUser;
import nl.codeclan.cvwiz.model.Cv;
import nl.codeclan.cvwiz.model.Experience;
import nl.codeclan.cvwiz.model.SkillMatrix;
import nl.codeclan.cvwiz.repository.CustomUserRepository;
import nl.codeclan.cvwiz.repository.CvRepository;
import nl.codeclan.cvwiz.repository.ExperienceRepository;
import nl.codeclan.cvwiz.repository.SkillMatrixRepository;
import nl.codeclan.cvwiz.service.ConsultantService;
import nl.codeclan.cvwiz.service.CustomUserService;
import nl.codeclan.cvwiz.service.CvService;
import nl.codeclan.cvwiz.service.ExperienceService;
import nl.codeclan.cvwiz.service.SkillMatrixService;
import nl.codeclan.cvwiz.support.RepositoryDoubles;
import nl.codeclan.cvwiz.support.TestData;
import nl.codeclan.cvwiz.support.TestPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsultantServiceTest {

    private RepositoryDoubles.ConsultantTestRepository consultantRepository;
    private RepositoryDoubles.TestRepository<CvRepository, Cv, Long> cvRepository;
    private RepositoryDoubles.TestRepository<ExperienceRepository, Experience, Long> experienceRepository;
    private RepositoryDoubles.TestRepository<SkillMatrixRepository, SkillMatrix, Long> skillMatrixRepository;
    private RepositoryDoubles.TestRepository<CustomUserRepository, CustomUser, String> userRepository;
    private ConsultantService service;

    @BeforeEach
    void setUp() {
        consultantRepository = RepositoryDoubles.consultants();
        cvRepository = RepositoryDoubles.cvs();
        experienceRepository = RepositoryDoubles.experiences();
        skillMatrixRepository = RepositoryDoubles.skillMatrices();
        userRepository = RepositoryDoubles.users();

        ExperienceService experienceService = new ExperienceService(experienceRepository.repository());
        SkillMatrixService skillMatrixService = new SkillMatrixService(skillMatrixRepository.repository());
        CvService cvService = new CvService(cvRepository.repository(), experienceService, skillMatrixService);
        CustomUserService customUserService = new CustomUserService(userRepository.repository(), new TestPasswordEncoder());
        service = new ConsultantService(consultantRepository.repository(), cvService, customUserService);
    }

    @Test
    void createsNewConsultantWithGeneratedUserAndUniqueId() {
        consultantRepository.queueExists(true, false);
        MedewerkerDto input = new MedewerkerDto("Jane", null, "Doe", "0612345678", "jane@example.com", TestData.cvDto(1L), List.of(TestData.cvDto(2L)));

        MedewerkerOnboardingResponseDto response = service.createNewConsultant(input);

        assertThat(response.consultant().getId()).isNotBlank();
        assertThat(response.consultant().getOrgineleCv()).isNull();
        assertThat(response.consultant().getCvLijst()).isEmpty();
        assertThat(response.username()).isEqualTo("jane@example.com");
        assertThat(response.oneTimePassword()).hasSize(20);
        assertThat(consultantRepository.savedEntities()).hasSize(1);
    }

    @Test
    void createsFallbackUsernameWhenEmailIsBlank() {
        MedewerkerDto input = new MedewerkerDto("Jane", null, "Doe", "0612345678", " ", null, List.of());

        MedewerkerOnboardingResponseDto response = service.createNewConsultant(input);

        assertThat(response.username()).startsWith("jane.doe.");
        assertThat(response.username()).doesNotContain(" ");
    }

    @Test
    void completesOneTimeCvAndKeepsUserEnabledForFollowUpUpdate() throws Exception {
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        CustomUser user = TestData.user("jane@example.com", "ROLE_CONSULTANT");
        Consultant consultant = TestData.consultant(TestData.CONSULTANT_ID, null, new ArrayList<>());
        consultant.setCustomUser(user);
        userRepository.put(user);
        consultantRepository.put(consultant);

        MedewerkerDto result = service.completeOneTimeCv("jane@example.com", TestData.cvDto(99L));

        assertThat(result.getOrgineleCv()).isNotNull();
        assertThat(result.getCvLijst()).hasSize(1);
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void completeOneTimeCvRejectsAlreadyCompletedOrMissingUsers() {
        CustomUser user = TestData.user("jane@example.com", "ROLE_CONSULTANT");
        Consultant consultant = TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), new ArrayList<>());
        consultant.setCustomUser(user);
        userRepository.put(user);
        consultantRepository.put(consultant);

        assertThrows(IllegalStateException.class, () -> service.completeOneTimeCv("jane@example.com", TestData.cvDto(1L)));
        assertThrows(FileNotFoundException.class, () -> service.completeOneTimeCv("missing@example.com", TestData.cvDto(1L)));
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void getsConsultantForOwningUserOnly() throws Exception {
        Consultant consultant = consultantForUser("jane@example.com", TestData.CONSULTANT_ID);
        consultantRepository.put(consultant);

        MedewerkerDto result = service.getConsultantForUser("jane@example.com", TestData.CONSULTANT_ID.toString());

        assertThat(result.getId()).isEqualTo(TestData.CONSULTANT_ID.toString());
        assertThrows(AccessDeniedException.class, () -> service.getConsultantForUser("jane@example.com", TestData.OTHER_CONSULTANT_ID.toString()));
    }

    @Test
    void updateOwnConsultantValidatesRequiredIdAndOwnership() throws Exception {
        Consultant consultant = consultantForUser("jane@example.com", TestData.CONSULTANT_ID);
        userRepository.put(consultant.getCustomUser());
        consultantRepository.put(consultant);
        cvRepository.put(TestData.cv(1L));
        MedewerkerDto missingId = TestData.consultantDto(null, TestData.cvDto(1L), List.of());
        MedewerkerDto wrongId = TestData.consultantDto(TestData.OTHER_CONSULTANT_ID, TestData.cvDto(1L), List.of());
        MedewerkerDto valid = TestData.consultantDto(TestData.CONSULTANT_ID, TestData.cvDto(1L), List.of());

        assertThrows(IllegalArgumentException.class, () -> service.updateOwnConsultant("jane@example.com", missingId));
        assertThrows(AccessDeniedException.class, () -> service.updateOwnConsultant("jane@example.com", wrongId));
        assertThat(service.updateOwnConsultant("jane@example.com", valid).getId()).isEqualTo(TestData.CONSULTANT_ID.toString());
    }

    @Test
    void updateOwnConsultantDisablesUserAfterSuccessfulUpdate() throws Exception {
        cvRepository.put(TestData.cv(1L));
        CustomUser user = TestData.user("jane@example.com", "ROLE_CONSULTANT");
        Consultant consultant = TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), new ArrayList<>());
        consultant.setCustomUser(user);
        userRepository.put(user);
        consultantRepository.put(consultant);
        MedewerkerDto update = TestData.consultantDto(TestData.CONSULTANT_ID, TestData.cvDto(1L), new ArrayList<>());

        MedewerkerDto result = service.updateOwnConsultant("jane@example.com", update);

        assertThat(result.getId()).isEqualTo(TestData.CONSULTANT_ID.toString());
        assertThat(consultantRepository.savedEntities()).hasSize(1);
        assertThat(consultantRepository.savedEntities().getFirst().getCustomUser()).isSameAs(user);
        assertThat(user.isEnabled()).isFalse();
        assertThat(userRepository.savedEntities()).contains(user);
    }

    @Test
    void updatesConsultantWithExistingAndNewUsedCvs() throws Exception {
        cvRepository.put(TestData.cv(1L));
        cvRepository.put(TestData.cv(2L));
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        Consultant consultant = TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), new ArrayList<>(List.of(TestData.cv(2L))));
        consultantRepository.put(consultant);
        CurriculumVitaeDto newCv = TestData.cvDto(99L);
        newCv.setId(null);
        MedewerkerDto update = TestData.consultantDto(
                TestData.CONSULTANT_ID,
                TestData.cvDto(1L),
                new ArrayList<>(List.of(TestData.cvDto(2L), newCv))
        );

        MedewerkerDto result = service.updateConsultant(update);

        assertThat(result.getCvLijst()).extracting(CurriculumVitaeDto::getId).containsExactly(2L, 3L);
        assertThat(result.getVoornaam()).isEqualTo("Jane");
        assertThat(cvRepository.find(3L)).isPresent();
    }

    @Test
    void updatesConsultantFieldsAndReturnsAdjustedValues() throws Exception {
        cvRepository.put(TestData.cv(1L));
        CurriculumVitaeDto updatedOriginalCv = TestData.cvDto(1L);
        updatedOriginalCv.setProfiel("Updated profile");
        Consultant consultant = new Consultant(
                TestData.CONSULTANT_ID,
                "Old",
                "Name",
                "0600000000",
                "old@example.com",
                TestData.cv(1L),
                new ArrayList<>()
        );
        consultantRepository.put(consultant);
        MedewerkerDto update = new MedewerkerDto(
                "New",
                TestData.CONSULTANT_ID.toString(),
                "Person",
                "0611111111",
                "new@example.com",
                updatedOriginalCv,
                new ArrayList<>()
        );

        MedewerkerDto result = service.updateConsultant(update);

        assertThat(result.getVoornaam()).isEqualTo("New");
        assertThat(result.getAchternaam()).isEqualTo("Person");
        assertThat(result.getTelefoon()).isEqualTo("0611111111");
        assertThat(result.getEmailAdres()).isEqualTo("new@example.com");
        assertThat(result.getOrgineleCv().getProfiel()).isEqualTo("Updated profile");
        assertThat(consultantRepository.savedEntities().getFirst().getFirstname()).isEqualTo("New");
    }

    @Test
    void keepsOriginalCvAndMatchingCvListEntryInSync() throws Exception {
        cvRepository.put(TestData.cv(1L));
        CurriculumVitaeDto updatedOriginalCv = TestData.cvDto(1L);
        updatedOriginalCv.setBestandsNaam("updated.pdf");
        updatedOriginalCv.setCompetenties(new ArrayList<>(List.of("Java", "Spring", "Angular")));
        updatedOriginalCv.setProfiel("Updated profile");
        updatedOriginalCv.setOpleiding("Updated education");
        CurriculumVitaeDto staleListCv = TestData.cvDto(1L);
        Consultant consultant = TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), new ArrayList<>(List.of(TestData.cv(1L))));
        consultantRepository.put(consultant);
        MedewerkerDto update = TestData.consultantDto(TestData.CONSULTANT_ID, updatedOriginalCv, new ArrayList<>(List.of(staleListCv)));

        MedewerkerDto result = service.updateConsultant(update);

        assertThat(result.getOrgineleCv().getBestandsNaam()).isEqualTo("updated.pdf");
        assertThat(result.getOrgineleCv().getCompetenties()).containsExactly("Java", "Spring", "Angular");
        assertThat(result.getOrgineleCv().getProfiel()).isEqualTo("Updated profile");
        assertThat(result.getOrgineleCv().getOpleiding()).isEqualTo("Updated education");
        assertThat(result.getCvLijst()).hasSize(1);
        assertThat(result.getCvLijst().getFirst().getBestandsNaam()).isEqualTo("updated.pdf");
        assertThat(result.getCvLijst().getFirst().getCompetenties()).containsExactly("Java", "Spring", "Angular");
        assertThat(result.getCvLijst().getFirst().getProfiel()).isEqualTo("Updated profile");
        assertThat(result.getCvLijst().getFirst().getOpleiding()).isEqualTo("Updated education");
        assertThat(cvRepository.find(1L).orElseThrow().getProfile()).isEqualTo("Updated profile");
    }

    @Test
    void updatesConsultantViaDirectSaveWhenUsedCvListIsUnchanged() throws Exception {
        cvRepository.put(TestData.cv(1L));
        Consultant consultant = TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), new ArrayList<>());
        consultantRepository.put(consultant);
        MedewerkerDto update = TestData.consultantDto(TestData.CONSULTANT_ID, TestData.cvDto(1L), new ArrayList<>());

        MedewerkerDto result = service.updateConsultant(update);

        assertThat(result.getCvLijst()).isEmpty();
        assertThat(consultantRepository.savedEntities()).hasSize(1);
    }

    @Test
    void updatesConsultantWithNullOriginalAndCvList() throws Exception {
        Consultant consultant = TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), List.of(TestData.cv(1L)));
        consultantRepository.put(consultant);
        MedewerkerDto update = TestData.consultantDto(TestData.CONSULTANT_ID, null, null);

        MedewerkerDto result = service.updateConsultant(update);

        assertThat(result.getOrgineleCv()).isNull();
        assertThat(result.getCvLijst()).isEmpty();
        assertThat(consultantRepository.savedEntities().getFirst().getOriginalCV()).isNull();
        assertThat(consultantRepository.savedEntities().getFirst().getUsedCvs()).isEmpty();
    }

    @Test
    void updateConsultantThrowsWhenMissing() {
        assertThrows(FileNotFoundException.class, () -> service.updateConsultant(TestData.consultantDto(TestData.CONSULTANT_ID, TestData.cvDto(1L), List.of())));
    }

    @Test
    void addsNewCvToUsedCvList() throws Exception {
        Consultant consultant = TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), new ArrayList<>(List.of(TestData.cv(1L))));
        consultantRepository.put(consultant);
        cvRepository.put(TestData.cv(1L));
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        CurriculumVitaeDto newCv = TestData.cvDto(10L);
        newCv.setMatrix(null);
        newCv.setErvaring(null);

        MedewerkerDto result = service.addNewCvToUsedCVList(TestData.CONSULTANT_ID.toString(), newCv);

        assertThat(result.getCvLijst()).hasSize(2);
        assertThat(result.getCvLijst().getLast().getMatrix()).isNotNull();
        assertThat(result.getCvLijst().getLast().getErvaring()).isEmpty();
        assertThat(cvRepository.find(2L)).isPresent();
    }

    @Test
    void addNewCvToUsedCvListThrowsWhenConsultantIsMissing() {
        assertThrows(FileNotFoundException.class, () -> service.addNewCvToUsedCVList(TestData.CONSULTANT_ID.toString(), TestData.cvDto(1L)));
    }

    @Test
    void getsConsultantByIdAndNameAndThrowsWhenMissing() throws Exception {
        Consultant consultant = TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), List.of(TestData.cv(2L)));
        consultantRepository.put(consultant);

        assertThat(service.getConsultant(TestData.CONSULTANT_ID.toString()).getEmailAdres()).isEqualTo("jane@example.com");
        assertThat(service.getConsultantByName("Jane", "Doe").getId()).isEqualTo(TestData.CONSULTANT_ID.toString());
        assertThat(service.getConsultantByName("jAnE", "dOE").getId()).isEqualTo(TestData.CONSULTANT_ID.toString());
        assertThrows(FileNotFoundException.class, () -> service.getConsultant(TestData.OTHER_CONSULTANT_ID.toString()));
        assertThrows(FileNotFoundException.class, () -> service.getConsultantByName("Missing", "Person"));
    }

    @Test
    void returnsConsultantUserNames() {
        consultantRepository.put(TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), List.of()));
        consultantRepository.put(new Consultant(TestData.OTHER_CONSULTANT_ID, "Ada", "Lovelace", "0611111111", "ada@example.com", null, List.of()));

        List<String> result = service.getUserNames();

        assertThat(result).containsExactly(
                "Jane Doe",
                "Ada Lovelace"
        );
    }

    @Test
    void deletesConsultantAndOwnedCvs() throws Exception {
        cvRepository.put(TestData.cv(1L));
        cvRepository.put(TestData.cv(2L));
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        skillMatrixRepository.put(TestData.skillMatrix(2L, "Backend", "Java"));
        experienceRepository.put(TestData.experience(1L));
        experienceRepository.put(TestData.experience(2L));
        MedewerkerDto dto = TestData.consultantDto(TestData.CONSULTANT_ID, null, List.of());
        consultantRepository.put(TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), List.of(TestData.cv(2L))));

        service.deleteConsultant(dto);

        assertThat(cvRepository.deletedIds()).containsExactly(2L, 1L);
        assertThat(consultantRepository.deletedIds()).containsExactly(TestData.CONSULTANT_ID);
    }

    @Test
    void deletesSharedOriginalAndUsedCvOnlyOnce() throws Exception {
        cvRepository.put(TestData.cv(1L));
        skillMatrixRepository.put(TestData.skillMatrix(1L, "Backend", "Java"));
        experienceRepository.put(TestData.experience(1L));
        MedewerkerDto dto = TestData.consultantDto(TestData.CONSULTANT_ID, TestData.cvDto(1L), List.of(TestData.cvDto(1L)));
        consultantRepository.put(TestData.consultant(TestData.CONSULTANT_ID, TestData.cv(1L), List.of(TestData.cv(1L))));

        service.deleteConsultant(dto);

        assertThat(cvRepository.deletedIds()).containsExactly(1L);
        assertThat(skillMatrixRepository.deletedIds()).containsExactly(1L);
        assertThat(experienceRepository.deletedIds()).containsExactly(1L);
        assertThat(consultantRepository.deletedIds()).containsExactly(TestData.CONSULTANT_ID);
    }

    @Test
    void deleteConsultantThrowsWhenMissing() {
        MedewerkerDto dto = TestData.consultantDto(TestData.CONSULTANT_ID, TestData.cvDto(1L), List.of());

        assertThrows(FileNotFoundException.class, () -> service.deleteConsultant(dto));
    }

    private Consultant consultantForUser(String username, UUID id) {
        CustomUser user = TestData.user(username, "ROLE_CONSULTANT");
        Consultant consultant = TestData.consultant(id, TestData.cv(1L), new ArrayList<>());
        consultant.setCustomUser(user);
        return consultant;
    }
}
