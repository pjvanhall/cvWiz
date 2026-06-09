package nl.codeclan.cvwiz.mapper;


import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.model.Cv;

import java.util.ArrayList;
import java.util.List;

public class CVMapper {


    public static Cv mapCVDtoToCV(CurriculumVitaeDto dto) {
        return new Cv(dto.getId(), dto.getBestandsNaam(), dto.getCompetenties(), dto.getProfiel(), dto.getOpleiding(), SkillMatrixMapper.mapDtoToSkillMatrix(dto.getMatrix()), ExperienceMapper.CollectorExperienceDtoListToExperienceList(dto.getErvaring()));
    }

    public static CurriculumVitaeDto mapCVToCVDto(Cv c) {
        return new CurriculumVitaeDto(c.getCvId(), c.getFileName(), c.getTechStack(), c.getProfile(), c.getEducation(), SkillMatrixMapper.mapSkillMatrixToDto(c.getSkillMatrix()), ExperienceMapper.CollectorExperienceListToExperienceDtoList(c.getExperience()));
    }

    public static List<Cv> CollectorCvDtoListToCvList(List<CurriculumVitaeDto> CurriculumViteaDtoList) {
        List<Cv> CvList = new ArrayList<>();
        for (CurriculumVitaeDto c : CurriculumViteaDtoList) {
            CvList.add(mapCVDtoToCV(c));
        }
        return CvList;
    }

    public static List<CurriculumVitaeDto> CollectorCvListToCvDtoList(List<Cv> cvlist) {
        List<CurriculumVitaeDto> dtos = new ArrayList<>();
        for (Cv c : cvlist) {
            dtos.add(mapCVToCVDto(c));
        }
        return dtos;
    }
}
