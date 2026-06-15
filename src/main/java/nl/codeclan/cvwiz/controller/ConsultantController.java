package nl.codeclan.cvwiz.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.service.ConsultantService;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/medewerkers")
@Validated
public class ConsultantController {

    private final ConsultantService consultantService;

    public ConsultantController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    @GetMapping("/medewerker")
//    @PreAuthorize("hasAuthority('ROLE_CONSULTANT')")
    public MedewerkerDto getConsultant(@RequestParam @NotBlank @Size(max = 36) String id, Authentication authentication) throws FileNotFoundException {
        if (authentication == null) {
            return consultantService.getConsultant(id);
        }
        return consultantService.getConsultantForUser(authentication.getName(), id);
    }

    @PostMapping("/updateMedewerker")
//    @PreAuthorize("hasAuthority('ROLE_CONSULTANT')")
    public MedewerkerDto updateConsultant(@Valid @RequestBody MedewerkerDto dto, Authentication authentication) throws FileNotFoundException {
        if (authentication == null) {
            return consultantService.updateConsultant(dto);
        }
        return consultantService.updateOwnConsultant(authentication.getName(), dto);
    }

    @PostMapping("/curriculumVitae/eersteLogin")
//    @PreAuthorize("hasAuthority('ROLE_CONSULTANT')")
    public MedewerkerDto completeOneTimeCv(@Valid @RequestBody CurriculumVitaeDto cvDto, Authentication authentication) throws FileNotFoundException {
        return consultantService.completeOneTimeCv(authentication.getName(), cvDto);
    }
}
