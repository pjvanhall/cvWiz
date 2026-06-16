package nl.codeclan.cvwiz.service;

import jakarta.persistence.EntityNotFoundException;
import nl.codeclan.cvwiz.dto.BeheerderDto;
import nl.codeclan.cvwiz.dto.MedewerkerOnboardingResponseDto;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.mapper.ManagerMapper;
import nl.codeclan.cvwiz.model.Manager;
import nl.codeclan.cvwiz.repository.ManagerRepository;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final ConsultantService consultantService;
    private final CustomUserService customUserService;

    public ManagerService(ManagerRepository managerRepository, ConsultantService consultantService, CustomUserService customUserService) {
        this.managerRepository = managerRepository;
        this.consultantService = consultantService;
        this.customUserService = customUserService;
    }

    public BeheerderDto createNewManager(BeheerderDto dto) {
        UUID id = UUID.randomUUID();
        while (managerRepository.existsById(id)) {
            id = UUID.randomUUID();
        }
        dto.setId(String.valueOf(id));
        Manager manager = ManagerMapper.managerDtoToManager(dto);
        manager.setCustomUser(customUserService.createGeneratedCustomUser(createUsername(dto.getEmailAdres(), dto.getVoornaam(), dto.getAchternaam(), id), dto.getEmailAdres(), "ROLE_MANAGER"));
        return ManagerMapper.managerToManagerDto(managerRepository.save(manager));
    }

    private String createUsername(String email, String firstname, String lastname, UUID id) {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return (firstname + "." + lastname + "." + id).replaceAll("\\s+", "").toLowerCase();
    }

    public BeheerderDto updateManager(BeheerderDto dto) throws EntityNotFoundException {
        if (managerRepository.existsById(UUID.fromString(dto.getId()))) {
            return ManagerMapper.managerToManagerDto(managerRepository.save(ManagerMapper.managerDtoToManager(dto)));
        } else {
            throw new EntityNotFoundException("Geen manager met id:" + dto.getId() + " gevonden.");
        }
    }

    public BeheerderDto getManager(UUID id) throws FileNotFoundException {
        if (managerRepository.existsById(id)) {
            return ManagerMapper.managerToManagerDto(managerRepository.getReferenceById(id));
        } else {
            throw new FileNotFoundException("Geen manager met dit id gevonden in de database.");
        }
    }

    public void deleteManager(UUID id) throws FileNotFoundException {
        if (managerRepository.existsById(id)) {
            managerRepository.deleteById(id);
        } else {
            throw new FileNotFoundException("Geen manager met dit id gevonden in de database.");
        }
    }

    public MedewerkerOnboardingResponseDto createNewConsultant(MedewerkerDto dto) {
        return consultantService.createNewConsultant(dto);
    }

    public MedewerkerDto updateConsultant(MedewerkerDto dto) throws FileNotFoundException {
        return consultantService.updateConsultant(dto);
    }

    public MedewerkerDto addNewCvToConsultantCvList(String consultantId, CurriculumVitaeDto cv) throws FileNotFoundException {
        return consultantService.addNewCvToUsedCVList(consultantId, cv);
    }

    public MedewerkerDto getConsultant(String firstname, String lastname) throws FileNotFoundException {
        return consultantService.getConsultantByName(firstname, lastname);
    }

    public void deleteConsultant(String firstname, String lastname) throws FileNotFoundException {
        consultantService.deleteConsultant(getConsultant(firstname, lastname));
    }

    public List<String> getUsers() {
        List<String> users = new ArrayList<>();
        for (Manager manager : managerRepository.findAll()) {
            users.add(manager.getFirstname() + " " + manager.getLastname());
        }
        users.addAll(consultantService.getUserNames());
        return users;
    }
}
