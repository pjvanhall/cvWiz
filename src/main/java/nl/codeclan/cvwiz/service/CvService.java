package nl.codeclan.cvwiz.service;


import jakarta.annotation.Nullable;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.mapper.CVMapper;
import nl.codeclan.cvwiz.mapper.ExperienceMapper;
import nl.codeclan.cvwiz.model.Cv;
import nl.codeclan.cvwiz.model.Experience;
import nl.codeclan.cvwiz.repository.CvRepository;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CvService {

    private final CvRepository repo;
    private final ExperienceService expSer;
    private final SkillMatrixService skms;

    public CvService(CvRepository repo, ExperienceService expSer, SkillMatrixService skms) {
        this.repo = repo;
        this.expSer = expSer;
        this.skms = skms;
    }


    public CurriculumVitaeDto createCVForNewConsultant(@Nullable CurriculumVitaeDto dto) throws FileNotFoundException {
        List<ErvaringDto> ervaringDtos = new ArrayList<>();
        ervaringDtos.add(expSer.createExperience(null));
        CurriculumVitaeDto newDto = new CurriculumVitaeDto();
        if(dto == null) {
            dto = newDto;
        }
        dto.setErvaring(ervaringDtos);
        dto.setMatrix(skms.createNewSkillMatrixOfBaseMatrix());
        dto.setId(repo.count()+1);
        return CVMapper.mapCVToCVDto(repo.save(CVMapper.mapCVDtoToCV(dto)));
    }

    public CurriculumVitaeDto createNewCv(CurriculumVitaeDto dto) {
        dto.setId(repo.count() + 1);
        expSer.createExperienceList(dto.getErvaring());
        return CVMapper.mapCVToCVDto(repo.save(CVMapper.mapCVDtoToCV(dto)));
    }

    public CurriculumVitaeDto getCvById(Long id) throws FileNotFoundException {
        if (repo.existsById(id)) {
            return CVMapper.mapCVToCVDto(repo.getReferenceById(id));
        } else {
            throw new FileNotFoundException("Er is geen bestaande cv gevonden in de database voor dit id");
        }
    }

    // In this method the matrix and experience list get checked, because those hold multiple values that can be updated. For the other fields this method overwrites the values instantly
    public CurriculumVitaeDto updateCV(CurriculumVitaeDto dto) throws FileNotFoundException {
        if (repo.existsById(dto.getId())) {
            CurriculumVitaeDto dbdto = CVMapper.mapCVToCVDto(repo.getReferenceById(dto.getId()));
            if (!dbdto.getMatrix().equals(dto.getMatrix())) {
                skms.updateSkillMatrix(dto.getMatrix());
            } else if (!dbdto.getErvaring().equals(dto.getErvaring())) {
                expSer.updateExperiencesList(dto.getErvaring());
            }
            return CVMapper.mapCVToCVDto(repo.save(CVMapper.mapCVDtoToCV(dto)));
        } else {
            throw new FileNotFoundException("De applicatie kan deze cv niet opslaan, omdat het geen bestaande id bevat");
        }
    }

    public void deleteCvById(Long id) throws FileNotFoundException {
        if (repo.existsById(id)) {
            Cv cv = repo.getReferenceById(id);
            skms.deleteSkillMatrix(cv.getSkillMatrix().getSkillMatrixId());
            for (Experience e : cv.getExperience()) {
                expSer.deleteExperience(ExperienceMapper.ExperienceToExperienceDto(e));
            }
            repo.deleteById(id);
        } else {
            throw new FileNotFoundException("Geen cv met dit id gevonden in de database.");
        }
    }

    public CurriculumVitaeDto updateOriginalCV(MedewerkerDto dto, CurriculumVitaeDto d) throws FileNotFoundException {
        if (repo.existsById(dto.getOrgineleCv().getId()) && dto.getOrgineleCv().getId().equals(d.getId())) {
            return CVMapper.mapCVToCVDto(repo.save(CVMapper.mapCVDtoToCV(d)));
        } else if (!dto.getOrgineleCv().getId().equals(d.getId())){
            throw new FileNotFoundException("Id van orginele cv en update cv komen niet overeen.");
        }else {
            throw new FileNotFoundException("Er is geen bestaande  cv gevonden in de database voor dit id");
        }
    }

    public boolean cvExist(Long l) {
        return repo.existsById(l);
    }
}
