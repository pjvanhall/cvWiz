package nl.codeclan.cvwiz.service;

import nl.codeclan.cvwiz.dto.MedewerkerOnboardingResponseDto;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.dto.MedewerkerListDto;
import nl.codeclan.cvwiz.mapper.CVMapper;
import nl.codeclan.cvwiz.mapper.ConsultantMapper;
import nl.codeclan.cvwiz.model.Consultant;
import nl.codeclan.cvwiz.model.Cv;
import nl.codeclan.cvwiz.repository.ConsultantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ConsultantService {

    private final ConsultantRepository consultantRepository;
    private final CvService cvService;
    private final CustomUserService customUserService;

    public ConsultantService(ConsultantRepository consultantRepository, CvService cvService, CustomUserService customUserService) {
        this.consultantRepository = consultantRepository;
        this.cvService = cvService;
        this.customUserService = customUserService;
    }

    public MedewerkerOnboardingResponseDto createNewConsultant(MedewerkerDto dto) {
        UUID id = UUID.randomUUID();
        while (consultantRepository.existsById(id)) {
            id = UUID.randomUUID();
        }
        dto.setId(String.valueOf(id));
        dto.setOrgineleCv(null);
        dto.setCvLijst(new ArrayList<>());
        Consultant consultant = ConsultantMapper.mapConsultantDtoToConsultant(dto);
        GeneratedCustomUser generatedUser = customUserService.createGeneratedCustomUserWithPassword(createUsername(dto.getEmailAdres(), dto.getVoornaam(), dto.getAchternaam(), id), dto.getEmailAdres(), "ROLE_CONSULTANT");
        consultant.setCustomUser(generatedUser.user());
        MedewerkerDto consultantDto = ConsultantMapper.mapConsultantToConsultantDto(consultantRepository.save(consultant));
        return new MedewerkerOnboardingResponseDto(consultantDto, generatedUser.user().getUsername(), generatedUser.oneTimePassword());
    }

    public MedewerkerDto completeOneTimeCv(String username, CurriculumVitaeDto cvDto) throws FileNotFoundException {
        Consultant consultant = getConsultantEntityForUser(username);

        if (consultant.getOriginalCV() != null) {
            customUserService.disableUser(username);
            throw new IllegalStateException("Deze onboarding is al afgerond.");
        }

        CurriculumVitaeDto savedCv = cvService.createCVForNewConsultant(cvDto);
        consultant.setOriginalCV(CVMapper.mapCVDtoToCV(savedCv));
        consultant.setUsedCvs(new ArrayList<>(List.of(CVMapper.mapCVDtoToCV(savedCv))));
        return ConsultantMapper.mapConsultantToConsultantDto(consultantRepository.save(consultant));
    }

    public MedewerkerDto getConsultantForUser(String username, String id) throws FileNotFoundException {
        Consultant consultant = getConsultantEntityForUser(username);
        UUID requestedId = UUID.fromString(id);
        if (!consultant.getConsultantId().equals(requestedId)) {
            throw new AccessDeniedException("Deze gebruiker heeft geen toegang tot deze consultant.");
        }
        return ConsultantMapper.mapConsultantToConsultantDto(consultant);
    }

    public MedewerkerDto updateOwnConsultant(String username, MedewerkerDto dto) throws FileNotFoundException {
        Consultant consultant = getConsultantEntityForUser(username);
        if (dto.getId() == null || dto.getId().isBlank()) {
            throw new IllegalArgumentException("Consultant id is verplicht.");
        }
        UUID requestedId = UUID.fromString(dto.getId());
        if (!consultant.getConsultantId().equals(requestedId)) {
            throw new AccessDeniedException("Deze gebruiker mag deze consultant niet wijzigen.");
        }
        MedewerkerDto updatedConsultant = updateConsultant(dto);
        return updatedConsultant;
    }

    private Consultant getConsultantEntityForUser(String username) throws FileNotFoundException {
        return consultantRepository.findByCustomUserUsername(username)
                .orElseThrow(() -> new FileNotFoundException("Geen consultant gevonden voor deze gebruiker."));
    }

    public MedewerkerDto getOwnConsultant(String username) throws FileNotFoundException {
        return ConsultantMapper.mapConsultantToConsultantDto(getConsultantEntityForUser(username));
    }

    private String createUsername(String email, String firstname, String lastname, UUID id) {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return (firstname + "." + lastname + "." + id).replaceAll("\\s+", "").toLowerCase();
    }

    public MedewerkerDto updateConsultant(MedewerkerDto dto) throws FileNotFoundException {
        UUID consultantId = UUID.fromString(dto.getId());
        if (!consultantRepository.existsById(consultantId)) {
            throw new FileNotFoundException("Geen medewerker met dit id gevonden in de database.");
        }

        Consultant consultant = consultantRepository.getReferenceById(consultantId);
        updateConsultantDetails(consultant, dto);
        CurriculumVitaeDto savedOriginalCv = saveCvDto(dto.getOrgineleCv());
        consultant.setOriginalCV(CVMapper.mapCVDtoToCV(savedOriginalCv));
        consultant.setUsedCvs(saveCvList(dto.getCvLijst(), savedOriginalCv));

        return ConsultantMapper.mapConsultantToConsultantDto(consultantRepository.save(consultant));
    }

    private void updateConsultantDetails(Consultant consultant, MedewerkerDto dto) {
        consultant.setEmail(dto.getEmailAdres());
        consultant.setFirstname(dto.getVoornaam());
        consultant.setLastname(dto.getAchternaam());
        consultant.setTelephone(dto.getTelefoon());
    }

    private CurriculumVitaeDto saveCvDto(CurriculumVitaeDto cvDto) throws FileNotFoundException {
        if (cvDto == null) {
            return null;
        }

        return cvService.cvExist(cvDto.getId())
                ? cvService.updateCV(cvDto)
                : cvService.createNewCv(cvDto);
    }

    private List<Cv> saveCvList(List<CurriculumVitaeDto> cvDtos, CurriculumVitaeDto savedOriginalCv) throws FileNotFoundException {
        List<Cv> savedCvs = new ArrayList<>();
        if (cvDtos == null) {
            return savedCvs;
        }
        for (CurriculumVitaeDto cvDto : cvDtos) {
            CurriculumVitaeDto savedCv = isSameCv(savedOriginalCv, cvDto)
                    ? savedOriginalCv
                    : saveCvDto(cvDto);
            if (savedCv != null) {
                savedCvs.add(CVMapper.mapCVDtoToCV(savedCv));
            }
        }
        return savedCvs;
    }

    private boolean isSameCv(CurriculumVitaeDto first, CurriculumVitaeDto second) {
        return first != null && second != null && first.getId() != null && first.getId().equals(second.getId());
    }

    public MedewerkerDto addNewCvToUsedCVList(String id, CurriculumVitaeDto cvDto) throws FileNotFoundException {
        UUID consultantId = UUID.fromString(id);
        if (!consultantRepository.existsById(consultantId)) {
            throw new FileNotFoundException("Geen consultant gevonden met deze gegevens.");
        }
        Consultant consultant = consultantRepository.getReferenceById(consultantId);
        MedewerkerDto dto = ConsultantMapper.mapConsultantToConsultantDto(consultant);
        List<CurriculumVitaeDto> list = new ArrayList<>(dto.getCvLijst());
        list.add(cvService.createNewCv(cvDto));
        consultant.setUsedCvs(CVMapper.CollectorCvDtoListToCvList(list));
        return ConsultantMapper.mapConsultantToConsultantDto(consultantRepository.save(consultant));
    }

    public MedewerkerDto getConsultant(String id) throws FileNotFoundException {
        if (consultantRepository.existsById(UUID.fromString(id))) {
            return ConsultantMapper.mapConsultantToConsultantDto(consultantRepository.getReferenceById(UUID.fromString(id)));
        } else {
            throw new FileNotFoundException("Geen consultant gevonden met deze gegevens.");
        }
    }

    @Transactional(readOnly = true)
    public List<MedewerkerListDto> getAllConsultants() {
        return consultantRepository.findAll().stream()
                .map(ConsultantMapper::mapConsultantToListDto)
                .toList();
    }

    public MedewerkerDto getConsultantByName(String firstname, String lastname) throws FileNotFoundException {
        Optional<Consultant> consultant = consultantRepository.findByFirstnameIgnoreCaseAndLastnameIgnoreCase(firstname, lastname);
        if (consultant.isPresent()) {
            return ConsultantMapper.mapConsultantToConsultantDto(consultant.get());
        } else {
            throw new FileNotFoundException("Geen consultant gevonden met deze gegevens.");
        }
    }

    public List<String> getUserNames() {
        return consultantRepository.findAll()
                .stream()
                .map(consultant -> (consultant.getFirstname() + " " + consultant.getLastname()))
                .toList();
    }

    @Transactional
    public void deleteConsultant(MedewerkerDto dto) throws FileNotFoundException {
        UUID consultantId = UUID.fromString(dto.getId());
        if (!consultantRepository.existsById(consultantId)) {
            throw new FileNotFoundException("Geen consultant gevonden met deze gegevens.");
        }

        Consultant consultant = consultantRepository.getReferenceById(consultantId);
        Set<Long> cvIds = collectOwnedCvIds(consultant);

        consultant.setOriginalCV(null);
        consultant.setUsedCvs(new ArrayList<>());
        consultantRepository.save(consultant);
        consultantRepository.flush();

        for (Long cvId : cvIds) {
            cvService.deleteCvById(cvId);
        }
        consultantRepository.deleteById(consultantId);
    }

    private Set<Long> collectOwnedCvIds(Consultant consultant) {
        Set<Long> cvIds = new LinkedHashSet<>();
        if (consultant.getUsedCvs() != null) {
            for (Cv cv : consultant.getUsedCvs()) {
                addCvId(cvIds, cv);
            }
        }
        addCvId(cvIds, consultant.getOriginalCV());
        return cvIds;
    }

    private void addCvId(Set<Long> cvIds, Cv cv) {
        if (cv != null && cv.getCvId() != null) {
            cvIds.add(cv.getCvId());
        }
    }
}
