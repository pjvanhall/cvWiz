package nl.codeclan.cvwiz.service;


import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.mapper.CVMapper;
import nl.codeclan.cvwiz.mapper.ConsultantMapper;
import nl.codeclan.cvwiz.model.Consultant;
import nl.codeclan.cvwiz.repository.ConsultantRepository;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConsultantService {

    private final ConsultantRepository repo;
    private final CvService cvs;


    public ConsultantService(ConsultantRepository cr, CvService cvs) {
        this.repo = cr;
        this.cvs = cvs;
    }

    //    Method to create a new Consultant with random UUID generation and check if the UUID isn't used already in database.
    public MedewerkerDto createNewConsultant(MedewerkerDto dto) throws FileNotFoundException {
        UUID id = UUID.randomUUID();
        while (repo.existsById(id)) {
            id = UUID.randomUUID();
        }
        dto.setId(String.valueOf(id));
        CurriculumVitaeDto cv = cvs.createCVForNewConsultant(dto.getOrgineleCv());
        dto.setOrgineleCv(cv);
        dto.setCvLijst(new ArrayList<>(List.of(cv)));
        return ConsultantMapper.mapConsultantToConsultantDto(repo.save(ConsultantMapper.mapConsultantDtoToConsultant(dto)));
    }

    public MedewerkerDto updateConsultant(MedewerkerDto dto) throws FileNotFoundException {
        if (repo.existsById(UUID.fromString(dto.getId()))) {
            Consultant consultant = repo.getReferenceById(UUID.fromString(dto.getId()));
            if (!consultant.getOriginalCV().equals(CVMapper.mapCVDtoToCV(dto.getOrgineleCv()))){
                cvs.updateOriginalCV(dto, dto.getOrgineleCv());
            }
            if (!consultant.getUsedCvs().equals(CVMapper.CollectorCvDtoListToCvList(dto.getCvLijst()))) {
                List<CurriculumVitaeDto> newList = new ArrayList<>();
                for (CurriculumVitaeDto cvd : dto.getCvLijst()) {
                    if(cvs.cvExist(cvd.getId()) && cvs.getCvById(cvd.getId())!=cvd){
                        newList.add(cvs.updateCV(cvd));
                    } else if (!cvs.cvExist(cvd.getId())) {
                        CurriculumVitaeDto cvdto =cvs.createNewCv(cvd);
                        cvd.setId(cvdto.getId());
                        newList.add(cvs.getCvById(cvdto.getId()));
                    }
                }
                consultant.setEmail(dto.getEmailAdres());
                consultant.setFirstname(dto.getVoornaam());
                consultant.setLastname(dto.getAchternaam());
                consultant.setTelephone(dto.getTelefoon());
                consultant.setUsedCvs(CVMapper.CollectorCvDtoListToCvList(newList));
                return ConsultantMapper.mapConsultantToConsultantDto(repo.save(consultant));
            } else {
                return ConsultantMapper.mapConsultantToConsultantDto(repo.save(ConsultantMapper.mapConsultantDtoToConsultant(dto)));
            }
        } else {
            throw new FileNotFoundException("Geen medewerker met dit id gevonden in de database.");
        }
    }

    public MedewerkerDto addNewCvToUsedCVList(String id, CurriculumVitaeDto cvDto) {
        Consultant c = repo.getReferenceById(UUID.fromString(id));
        MedewerkerDto dto = ConsultantMapper.mapConsultantToConsultantDto(c);
        List<CurriculumVitaeDto> list = new ArrayList<>(dto.getCvLijst());
        list.add(cvs.createNewCv(cvDto));
        c.setUsedCvs(CVMapper.CollectorCvDtoListToCvList(list));
        return ConsultantMapper.mapConsultantToConsultantDto(repo.save(c));
    }

    public MedewerkerDto getConsultant(String id) throws FileNotFoundException {
        if (repo.existsById(UUID.fromString(id))) {
            return ConsultantMapper.mapConsultantToConsultantDto(repo.getReferenceById(UUID.fromString(id)));
        } else {
            throw new FileNotFoundException("Geen consultant gevonden met deze gegevens.");
        }
    }

    public MedewerkerDto getConsultantByName(String firstname, String lastname) throws FileNotFoundException {
        Optional<Consultant> con = repo.getByFirstnameAndLastname(firstname, lastname);
        if (con.isPresent()) {
            return ConsultantMapper.mapConsultantToConsultantDto(con.get());
        } else {
            throw new FileNotFoundException("Geen consultant gevonden met deze gegevens.");
        }
    }

    public void deleteConsultant(MedewerkerDto dto) throws FileNotFoundException {
        if (repo.existsById(UUID.fromString(dto.getId()))) {
            for (CurriculumVitaeDto cv : dto.getCvLijst()) {
                cvs.deleteCvById(cv.getId());
            }
            cvs.deleteCvById(dto.getOrgineleCv().getId());
            repo.deleteById(UUID.fromString(dto.getId()));
        } else {
            throw new FileNotFoundException("Geen consultant gevonden met deze gegevens.");
        }
    }
}
