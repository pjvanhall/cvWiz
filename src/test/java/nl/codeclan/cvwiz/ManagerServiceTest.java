package nl.codeclan.cvwiz;

import jakarta.persistence.EntityNotFoundException;
import nl.codeclan.cvwiz.dto.BeheerderDto;
import nl.codeclan.cvwiz.dto.MedewerkerOnboardingResponseDto;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.model.CustomUser;
import nl.codeclan.cvwiz.model.Manager;
import nl.codeclan.cvwiz.repository.CustomUserRepository;
import nl.codeclan.cvwiz.repository.ManagerRepository;
import nl.codeclan.cvwiz.service.ConsultantService;
import nl.codeclan.cvwiz.service.CustomUserService;
import nl.codeclan.cvwiz.service.ManagerService;
import nl.codeclan.cvwiz.support.RepositoryDoubles;
import nl.codeclan.cvwiz.support.TestData;
import nl.codeclan.cvwiz.support.TestPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManagerServiceTest {

    private RepositoryDoubles.TestRepository<ManagerRepository, Manager, java.util.UUID> managerRepository;
    private RecordingConsultantService consultantService;
    private ManagerService service;

    @BeforeEach
    void setUp() {
        managerRepository = RepositoryDoubles.managers();
        RepositoryDoubles.TestRepository<CustomUserRepository, CustomUser, String> userRepository = RepositoryDoubles.users();
        CustomUserService customUserService = new CustomUserService(userRepository.repository(), new TestPasswordEncoder());
        consultantService = new RecordingConsultantService();
        service = new ManagerService(managerRepository.repository(), consultantService, customUserService);
    }

    @Test
    void createsNewManagerWithUniqueIdAndEmailUsername() {
        managerRepository.queueExists(true, false);
        BeheerderDto input = new BeheerderDto("John", null, "Manager", "0698765432", "john.manager@example.com");

        BeheerderDto result = service.createNewManager(input);

        Manager saved = managerRepository.savedEntities().getFirst();
        assertThat(result.getId()).isNotBlank();
        assertThat(saved.getCustomUser().getUsername()).isEqualTo("john.manager@example.com");
        assertThat(saved.getCustomUser().getAuthorisaties()).extracting(auth -> auth.getAuthorisatie()).containsExactly("ROLE_MANAGER");
    }

    @Test
    void createsFallbackUsernameWhenManagerEmailIsBlank() {
        BeheerderDto input = new BeheerderDto("John", null, "Manager", "0698765432", " ");

        service.createNewManager(input);

        String username = managerRepository.savedEntities().getFirst().getCustomUser().getUsername();
        assertThat(username).startsWith("john.manager.");
        assertThat(username).doesNotContain(" ");
    }

    @Test
    void updatesGetsAndDeletesExistingManager() throws Exception {
        managerRepository.put(TestData.manager(TestData.MANAGER_ID));

        BeheerderDto updated = service.updateManager(TestData.managerDto(TestData.MANAGER_ID));
        BeheerderDto found = service.getManager(TestData.MANAGER_ID);
        service.deleteManager(TestData.MANAGER_ID);

        assertThat(updated.getId()).isEqualTo(TestData.MANAGER_ID.toString());
        assertThat(found.getEmailAdres()).isEqualTo("john.manager@example.com");
        assertThat(managerRepository.deletedIds()).containsExactly(TestData.MANAGER_ID);
    }

    @Test
    void managerOperationsThrowWhenMissing() {
        assertThrows(EntityNotFoundException.class, () -> service.updateManager(TestData.managerDto(TestData.MANAGER_ID)));
        assertThrows(FileNotFoundException.class, () -> service.getManager(TestData.MANAGER_ID));
        assertThrows(FileNotFoundException.class, () -> service.deleteManager(TestData.MANAGER_ID));
    }

    @Test
    void delegatesConsultantWorkflows() throws Exception {
        MedewerkerDto consultant = TestData.consultantDto(TestData.CONSULTANT_ID, TestData.cvDto(1L), List.of());
        CurriculumVitaeDto cv = TestData.cvDto(2L);

        MedewerkerOnboardingResponseDto created = service.createNewConsultant(consultant);
        MedewerkerDto updated = service.updateConsultant(consultant);
        MedewerkerDto withCv = service.addNewCvToConsultantCvList(TestData.CONSULTANT_ID.toString(), cv);
        MedewerkerDto found = service.getConsultant("Jane", "Doe");
        service.deleteConsultant("Jane", "Doe");

        assertThat(created.consultant()).isSameAs(consultant);
        assertThat(updated).isSameAs(consultant);
        assertThat(withCv).isSameAs(consultant);
        assertThat(found).isSameAs(consultant);
        assertThat(consultantService.deleted).isSameAs(consultant);
        assertThat(consultantService.addedCv).isSameAs(cv);
    }

    @Test
    void getsManagerAndConsultantNames() {
        managerRepository.put(TestData.manager(TestData.MANAGER_ID));
        consultantService.userNames = List.of("Jane Doe");

        List<String> result = service.getUsers();

        assertThat(result).containsExactly(
                ("John Manager"),
                ("Jane Doe")
        );
    }

    private static final class RecordingConsultantService extends ConsultantService {
        private MedewerkerDto consultant;
        private CurriculumVitaeDto addedCv;
        private MedewerkerDto deleted;
        private List<String> userNames = List.of();

        private RecordingConsultantService() {
            super(null, null, null);
        }

        @Override
        public MedewerkerOnboardingResponseDto createNewConsultant(MedewerkerDto dto) {
            consultant = dto;
            return new MedewerkerOnboardingResponseDto(dto, "username", "password");
        }

        @Override
        public MedewerkerDto updateConsultant(MedewerkerDto dto) {
            consultant = dto;
            return dto;
        }

        @Override
        public MedewerkerDto addNewCvToUsedCVList(String id, CurriculumVitaeDto cvDto) {
            addedCv = cvDto;
            return consultant;
        }

        @Override
        public MedewerkerDto getConsultantByName(String firstname, String lastname) {
            return consultant;
        }

        @Override
        public void deleteConsultant(MedewerkerDto dto) {
            deleted = dto;
        }

        @Override
        public List<String> getUserNames() {
            return userNames;
        }
    }
}
