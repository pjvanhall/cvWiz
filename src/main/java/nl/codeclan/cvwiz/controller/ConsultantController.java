package nl.codeclan.cvwiz.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.dto.MedewerkerListDto;
import nl.codeclan.cvwiz.service.ConsultantService;
import nl.codeclan.cvwiz.service.SkillMatrixService;
import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/medewerkers")
@Validated
public class ConsultantController {

    private final ConsultantService consultantService;
    private final SkillMatrixService skillMatrixService;

    public ConsultantController(ConsultantService consultantService, SkillMatrixService skillMatrixService) {
        this.consultantService = consultantService;
        this.skillMatrixService = skillMatrixService;
    }

    @GetMapping("/medewerker")
    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTANT', 'ROLE_MANAGER')")
    public MedewerkerDto getConsultant(@RequestParam @NotBlank @Size(max = 36) String id, Authentication authentication) throws FileNotFoundException {

        try {
            return consultantService.getConsultantForUser(authentication.getName(), id);
        } catch (FileNotFoundException e) {
            if ("Geen consultant gevonden voor deze gebruiker.".equals(e.getMessage())) {
                return consultantService.getConsultant(id);
            }
            throw e;
        }
    }

    @GetMapping("/alle")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<MedewerkerListDto> getAllConsultants() {
        return consultantService.getAllConsultants();
    }

    @PostMapping("/updateMedewerker")
    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTANT', 'ROLE_MANAGER')")
    public MedewerkerDto updateConsultant(@Valid @RequestBody MedewerkerDto dto, Authentication authentication) throws FileNotFoundException {

        try {
            return consultantService.updateOwnConsultant(authentication.getName(), dto);
        } catch (FileNotFoundException e) {
            if ("Geen consultant gevonden voor deze gebruiker.".equals(e.getMessage())) {
                return consultantService.updateConsultant(dto);
            }
            throw e;
        }
    }

    @PostMapping("/curriculumVitae/eersteLogin")
    @PreAuthorize("hasAuthority('ROLE_CONSULTANT')")
    public MedewerkerDto completeOneTimeCv(@Valid @RequestBody CurriculumVitaeDto cvDto, Authentication authentication) throws FileNotFoundException {
        return consultantService.completeOneTimeCv(authentication.getName(), cvDto);
    }

    @GetMapping("/mijzelf")
    @PreAuthorize("hasAuthority('ROLE_CONSULTANT')")
    public MedewerkerDto getOwnProfile(Authentication authentication) throws FileNotFoundException {
        return consultantService.getOwnConsultant(authentication.getName());
    }

    @GetMapping("/basisMatrix")
    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTANT', 'ROLE_MANAGER')")
    public TechniekMatrixDto getBaseMatrix() throws FileNotFoundException {
        return skillMatrixService.getSkillMatrix(1L);
    }
}
