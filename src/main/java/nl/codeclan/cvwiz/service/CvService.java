package nl.codeclan.cvwiz.service;

import jakarta.annotation.Nullable;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.mapper.CVMapper;
import nl.codeclan.cvwiz.mapper.ExperienceMapper;
import nl.codeclan.cvwiz.model.Cv;
import nl.codeclan.cvwiz.model.Experience;
import nl.codeclan.cvwiz.repository.CvRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CvService {

    private final CvRepository cvRepository;
    private final ExperienceService experienceService;
    private final SkillMatrixService skillMatrixService;

    public CvService(CvRepository cvRepository, ExperienceService experienceService, SkillMatrixService skillMatrixService) {
        this.cvRepository = cvRepository;
        this.experienceService = experienceService;
        this.skillMatrixService = skillMatrixService;
    }

    public CurriculumVitaeDto createCVForNewConsultant(@Nullable CurriculumVitaeDto dto) throws FileNotFoundException {
        CurriculumVitaeDto cv = dto == null ? new CurriculumVitaeDto() : dto;
        cv.setErvaring(new ArrayList<>(List.of(experienceService.createExperience(null))));
        return saveCreatedCv(cv);
    }

    public CurriculumVitaeDto createNewCv(CurriculumVitaeDto dto) throws FileNotFoundException {
        CurriculumVitaeDto cv = dto == null ? new CurriculumVitaeDto() : dto;
        cv.setErvaring(experienceService.createExperienceList(cv.getErvaring()));
        return saveCreatedCv(cv);
    }

    public CurriculumVitaeDto getCvById(Long id) throws FileNotFoundException {
        if (cvRepository.existsById(id)) {
            return CVMapper.mapCVToCVDto(cvRepository.getReferenceById(id));
        } else {
            throw new FileNotFoundException("Er is geen bestaande cv gevonden in de database voor dit id");
        }
    }

    public CurriculumVitaeDto updateCV(CurriculumVitaeDto dto) throws FileNotFoundException {
        if (dto == null || dto.getId() == null || !cvRepository.existsById(dto.getId())) {
            throw new FileNotFoundException("De applicatie kan deze cv niet opslaan, omdat het geen bestaande id bevat");
        }
        CurriculumVitaeDto existingCv = CVMapper.mapCVToCVDto(cvRepository.getReferenceById(dto.getId()));
        saveChangedAssociations(dto, existingCv);
        return saveCv(dto);
    }

    @Transactional
    public void deleteCvById(Long id) throws FileNotFoundException {
        if (cvRepository.existsById(id)) {
            Cv cv = cvRepository.getReferenceById(id);
            Long skillMatrixId = cv.getSkillMatrix() == null ? null : cv.getSkillMatrix().getSkillMatrixId();
            List<Experience> experiences = cv.getExperience() == null ? List.of() : new ArrayList<>(cv.getExperience());

            cvRepository.deleteById(id);
            cvRepository.flush();

            if (skillMatrixId != null) {
                skillMatrixService.deleteSkillMatrix(skillMatrixId);
            }
            for (Experience e : experiences) {
                experienceService.deleteExperience(ExperienceMapper.ExperienceToExperienceDto(e));
            }
        } else {
            throw new FileNotFoundException("Geen cv met dit id gevonden in de database.");
        }
    }

    public CurriculumVitaeDto updateOriginalCV(MedewerkerDto dto, CurriculumVitaeDto updatedCv) throws FileNotFoundException {
        Long originalCvId = dto == null || dto.getOrgineleCv() == null ? null : dto.getOrgineleCv().getId();
        Long updatedCvId = updatedCv == null ? null : updatedCv.getId();
        if (originalCvId == null || updatedCvId == null) {
            throw new FileNotFoundException("Er is geen bestaande  cv gevonden in de database voor dit id");
        }
        if (!originalCvId.equals(updatedCvId)) {
            throw new FileNotFoundException("Id van orginele cv en update cv komen niet overeen.");
        }
        if (!cvRepository.existsById(originalCvId)) {
            throw new FileNotFoundException("Er is geen bestaande  cv gevonden in de database voor dit id");
        }

        CurriculumVitaeDto existingCv = CVMapper.mapCVToCVDto(cvRepository.getReferenceById(updatedCvId));
        saveChangedAssociations(updatedCv, existingCv);
        return saveCv(updatedCv);
    }

    public boolean cvExist(Long id) {
        return id != null && cvRepository.existsById(id);
    }

    private CurriculumVitaeDto saveCreatedCv(CurriculumVitaeDto dto) throws FileNotFoundException {
        dto.setId(nextCvId());
        dto.setMatrix(skillMatrixService.saveSubmittedSkillMatrix(dto.getMatrix()));
        return saveCv(dto);
    }

    private void saveChangedAssociations(CurriculumVitaeDto updatedCv, CurriculumVitaeDto existingCv) throws FileNotFoundException {
        if (!Objects.equals(existingCv.getMatrix(), updatedCv.getMatrix())) {
            updatedCv.setMatrix(skillMatrixService.saveSubmittedSkillMatrix(updatedCv.getMatrix()));
        }
        if (!Objects.equals(existingCv.getErvaring(), updatedCv.getErvaring())) {
            updatedCv.setErvaring(experienceService.saveSubmittedExperiencesList(updatedCv.getErvaring()));
        }
    }

    private CurriculumVitaeDto saveCv(CurriculumVitaeDto dto) {
        return CVMapper.mapCVToCVDto(cvRepository.save(CVMapper.mapCVDtoToCV(dto)));
    }

    private Long nextCvId() {
        long id = cvRepository.count() + 1;
        while (cvRepository.existsById(id)) {
            id++;
        }
        return id;
    }
}
