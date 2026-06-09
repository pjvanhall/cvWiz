package nl.codeclan.cvwiz.controller;


import nl.codeclan.cvwiz.dto.BeheerderDto;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.service.ConsultantService;
import nl.codeclan.cvwiz.service.CvService;
import nl.codeclan.cvwiz.service.ManagerService;
import nl.codeclan.cvwiz.service.SkillMatrixService;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.UUID;

@RestController
@RequestMapping("/beheerders")
public class ManagerController {

    private final ManagerService manSer;
    private final SkillMatrixService skiMatSer;
    private final CvService cvSer;
    private final ConsultantService consultantService;

    public ManagerController(ManagerService manSer, SkillMatrixService skiMatSer, CvService cvSer, ConsultantService consultantService) {
        this.manSer = manSer;
        this.skiMatSer = skiMatSer;
        this.cvSer = cvSer;
        this.consultantService = consultantService;
    }

    @GetMapping("/beheerder")
    public BeheerderDto getBeheerderDto(@RequestParam UUID id) throws FileNotFoundException {
        return manSer.getManager(id);
    }

    @PostMapping("/nieuw")
    public BeheerderDto createBeheerderDto(@RequestBody BeheerderDto dto) {
        return manSer.createNewManager(dto);
    }

    @PostMapping("/bewerk")
    public BeheerderDto updateBeheerderDto(@RequestBody BeheerderDto dto) {
        return manSer.updateManager(dto);
    }

    @DeleteMapping("/verwijder")
    public void deleteBeheerderDto(@RequestParam UUID id) throws FileNotFoundException {
        manSer.deleteManager(id);
    }

    @PostMapping("/nieuweMedewerker")
    public MedewerkerDto createMedewerkerDto(@RequestBody MedewerkerDto dto) throws FileNotFoundException {
        return manSer.createNewConsultant(dto);
    }

    @PostMapping("/updateMedewerker")
    public MedewerkerDto updateMedewerkerDto(@RequestBody MedewerkerDto dto) throws FileNotFoundException {
        return manSer.updateConsultant(dto);
    }

    @PostMapping("/nieuweCurriculumVitae")
    public MedewerkerDto nieuweCurriculumVitaeDto(@RequestParam String id, @RequestBody CurriculumVitaeDto cv) {
        return manSer.addNewCvToConsultantCvList(id, cv);
    }

    @DeleteMapping("/deleteMedewerker")
    public void deleteMedewerkerDto(@RequestParam String firstname, @RequestParam String lastname) throws FileNotFoundException {
        manSer.deleteConsultant(firstname, lastname);
    }

    @PostMapping("/nieweCategory")
    public String addNewCategory(@RequestParam String category, @RequestParam String techniek) throws FileNotFoundException {
        return skiMatSer.addNewCategoryToMapCategories(category, techniek);
    }

    @PostMapping("/nieuweTechniek")
    public String addNewTool(@RequestParam String category, @RequestParam String techniek) throws FileNotFoundException {
        return skiMatSer.addNewToolToMapCategories(category, techniek);
    }

    @GetMapping("/matrix")
    public TechniekMatrixDto getTechniekMatrixDto(@RequestParam Long id) throws FileNotFoundException {
        return skiMatSer.getSkillMatrix(id);
    }

    @GetMapping("/curriculumVitae")
    public CurriculumVitaeDto getCurriculumViteaDto(@RequestParam Long id) throws FileNotFoundException {
        return cvSer.getCvById(id);
    }

    @PostMapping("/curriculumVitae/update")
    public CurriculumVitaeDto updateCurriculumViteaDto(@RequestBody CurriculumVitaeDto dto) throws FileNotFoundException {
        return cvSer.updateCV(dto);
    }

    @PostMapping("/curriculumVitae/update/originale")
    public CurriculumVitaeDto updateOiginalCurriculumViteaDto(@RequestParam String voornaam, @RequestParam String achternaam, @RequestBody CurriculumVitaeDto d) throws FileNotFoundException {
        return cvSer.updateOriginalCV(consultantService.getConsultantByName(voornaam, achternaam), d);
    }
}
