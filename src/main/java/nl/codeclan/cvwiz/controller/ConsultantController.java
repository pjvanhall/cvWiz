package nl.codeclan.cvwiz.controller;


import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.service.ConsultantService;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/medewerkers")
public class ConsultantController {

    private final ConsultantService conSer;

    public ConsultantController(ConsultantService conSer) {
        this.conSer = conSer;
    }

    @GetMapping("/medewerker")
    public MedewerkerDto getConsultant(@RequestParam String id) throws FileNotFoundException {
        return conSer.getConsultant(id);
    }

    @PostMapping("/updateMedewerker")
    public MedewerkerDto updateConsultant(@RequestBody MedewerkerDto dto) throws FileNotFoundException {
        return conSer.updateConsultant(dto);
    }
}
