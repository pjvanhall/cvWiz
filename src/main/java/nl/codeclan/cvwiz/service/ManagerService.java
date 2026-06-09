package nl.codeclan.cvwiz.service;


import jakarta.persistence.EntityNotFoundException;
import nl.codeclan.cvwiz.dto.BeheerderDto;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.mapper.ManagerMapper;
import nl.codeclan.cvwiz.repository.ManagerRepository;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.UUID;

@Service
public class ManagerService {

    private final ManagerRepository repo;
    private final ConsultantService conSer;

    public ManagerService(ManagerRepository repo, ConsultantService conSer) {
        this.repo = repo;
        this.conSer = conSer;
    }

    public BeheerderDto createNewManager(BeheerderDto dto) {
        UUID id = UUID.randomUUID();
        while (repo.existsById(id)) {
            id = UUID.randomUUID();
        }
        dto.setId(String.valueOf(id));
        return ManagerMapper.managerToManagerDto(repo.save(ManagerMapper.managerDtoToManager(dto)));
    }

    public BeheerderDto updateManager(BeheerderDto dto) throws EntityNotFoundException {
        if(repo.existsById(UUID.fromString(dto.getId()))) {
            return ManagerMapper.managerToManagerDto(repo.save(ManagerMapper.managerDtoToManager(dto)));
        } else {
            throw new EntityNotFoundException("Geen manager met id:" + dto.getId() + " gevonden.");
        }
    }

    public BeheerderDto getManager(UUID id) throws FileNotFoundException {
        if (repo.existsById(id)) {
            return ManagerMapper.managerToManagerDto(repo.getReferenceById(id));
        } else {
            throw new FileNotFoundException("Geen manager met dit id gevonden in de database.");
        }
    }

    public void deleteManager(UUID id) throws FileNotFoundException {
        if (repo.existsById(id)) {
            repo.deleteById(id);
        } else {
            throw new FileNotFoundException("Geen manager met dit id gevonden in de database.");
        }
    }

    public MedewerkerDto createNewConsultant(MedewerkerDto dto) throws FileNotFoundException {
        return conSer.createNewConsultant(dto);
    }

    public MedewerkerDto updateConsultant(MedewerkerDto dto) throws FileNotFoundException {
        return conSer.updateConsultant(dto);
    }

    public MedewerkerDto addNewCvToConsultantCvList(String id, CurriculumVitaeDto cv) {
        return conSer.addNewCvToUsedCVList(id, cv);
    }

    public MedewerkerDto getConsultant(String firstname, String lastname) throws FileNotFoundException {
        return conSer.getConsultantByName(firstname, lastname);
    }

    public void deleteConsultant(String firstname, String lastname) throws FileNotFoundException {
        conSer.deleteConsultant(getConsultant(firstname, lastname));
    }
}
