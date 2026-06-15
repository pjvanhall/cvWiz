package nl.codeclan.cvwiz.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import nl.codeclan.cvwiz.dto.*;
import nl.codeclan.cvwiz.service.ConsultantService;
import nl.codeclan.cvwiz.service.CvService;
import nl.codeclan.cvwiz.service.ManagerService;
import nl.codeclan.cvwiz.service.SkillMatrixService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/beheerders")
// @PreAuthorize("hasAuthority('ROLE_MANAGER')")
@Validated
public class ManagerController {

    private final ManagerService managerService;
    private final SkillMatrixService skillMatrixService;
    private final CvService cvService;
    private final ConsultantService consultantService;

    public ManagerController(
            ManagerService managerService,
            SkillMatrixService skillMatrixService,
            CvService cvService,
            ConsultantService consultantService
    ) {
        this.managerService = managerService;
        this.skillMatrixService = skillMatrixService;
        this.cvService = cvService;
        this.consultantService = consultantService;
    }

    @GetMapping("/beheerder")
    public BeheerderDto getManager(@RequestParam UUID id) throws FileNotFoundException {
        return managerService.getManager(id);
    }

    @PostMapping("/nieuw")
    public BeheerderDto createManager(@Valid @RequestBody BeheerderDto dto) {
        return managerService.createNewManager(dto);
    }

    @PostMapping("/bewerk")
    public BeheerderDto updateManager(@Valid @RequestBody BeheerderDto dto) {
        return managerService.updateManager(dto);
    }

    @DeleteMapping("/verwijder")
    public void deleteManager(@RequestParam UUID id) throws FileNotFoundException {
        managerService.deleteManager(id);
    }

    @PostMapping("/nieuweMedewerker")
    public MedewerkerOnboardingResponseDto createConsultant(@Valid @RequestBody MedewerkerDto dto) {
        return managerService.createNewConsultant(dto);
    }

    @PostMapping("/updateMedewerker")
    public MedewerkerDto updateConsultant(@Valid @RequestBody MedewerkerDto dto) throws FileNotFoundException {
        return managerService.updateConsultant(dto);
    }

    @PostMapping("/nieuweCurriculumVitae")
    public MedewerkerDto addCurriculumVitaeToConsultant(
            @RequestParam String id,
            @Valid @RequestBody CurriculumVitaeDto cv
    ) throws FileNotFoundException {
        return managerService.addNewCvToConsultantCvList(id, cv);
    }

    @DeleteMapping("/deleteMedewerker")
    public void deleteConsultant(
            @RequestParam @NotBlank @Size(max = 100) String voornaam,
            @RequestParam @NotBlank @Size(max = 100) String achternaam
    ) throws FileNotFoundException {
        managerService.deleteConsultant(voornaam, achternaam);
    }

    @PostMapping({"/nieuweCategory", "/nieweCategory"})
    public String addNewCategory(
            @RequestParam @NotBlank @Size(max = 100) String category,
            @RequestParam @NotBlank @Size(max = 100) String techniek
    ) throws FileNotFoundException {
        return skillMatrixService.addNewCategoryToMapCategories(category, techniek);
    }

    @PostMapping("/nieuweTechniek")
    public String addNewTool(
            @RequestParam @NotBlank @Size(max = 100) String category,
            @RequestParam @NotBlank @Size(max = 100) String techniek
    ) throws FileNotFoundException {
        return skillMatrixService.addNewToolToMapCategories(category, techniek);
    }

    @GetMapping("/matrix")
    public TechniekMatrixDto getSkillMatrix(@RequestParam Long id) throws FileNotFoundException {
        return skillMatrixService.getSkillMatrix(id);
    }

    @GetMapping("/curriculumVitae")
    public CurriculumVitaeDto getCurriculumVitae(@RequestParam Long id) throws FileNotFoundException {
        return cvService.getCvById(id);
    }

    @PostMapping("/curriculumVitae/update")
    public CurriculumVitaeDto updateCurriculumVitae(@Valid @RequestBody CurriculumVitaeDto dto) throws FileNotFoundException {
        return cvService.updateCV(dto);
    }

    @PostMapping("/curriculumVitae/update/originale")
    public CurriculumVitaeDto updateOriginalCurriculumVitae(
            @RequestParam @NotBlank @Size(max = 100) String voornaam,
            @RequestParam @NotBlank @Size(max = 100) String achternaam,
            @Valid @RequestBody CurriculumVitaeDto dto
    ) throws FileNotFoundException {
        return cvService.updateOriginalCV(consultantService.getConsultantByName(voornaam, achternaam), dto);
    }

    @GetMapping("/gebruikers")
    public List<String> getUsers() {
        return managerService.getUsers();
    }
}
