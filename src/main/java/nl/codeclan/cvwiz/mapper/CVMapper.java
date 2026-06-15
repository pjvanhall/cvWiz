package nl.codeclan.cvwiz.mapper;

import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.model.Cv;

import java.util.ArrayList;
import java.util.List;

public class CVMapper {

    public static Cv mapCVDtoToCV(CurriculumVitaeDto dto) {
        if (dto == null) {
            return null;
        }
        return new Cv(dto.getId(), dto.getBestandsNaam(), dto.getCompetenties(), dto.getLanguages(), dto.getProfiel(), dto.getOpleiding(), SkillMatrixMapper.mapDtoToSkillMatrix(dto.getMatrix()), ExperienceMapper.CollectorExperienceDtoListToExperienceList(dto.getErvaring()));
    }

    public static CurriculumVitaeDto mapCVToCVDto(Cv c) {
        if (c == null) {
            return null;
        }
        return new CurriculumVitaeDto(c.getCvId(), c.getFileName(), c.getTechStack(), c.getLanguages(), c.getProfile(), c.getEducation(), SkillMatrixMapper.mapSkillMatrixToDto(c.getSkillMatrix()), ExperienceMapper.CollectorExperienceListToExperienceDtoList(c.getExperience()));
    }

    public static List<Cv> CollectorCvDtoListToCvList(List<CurriculumVitaeDto> curriculumVitaeDtoList) {
        List<Cv> cvList = new ArrayList<>();
        if (curriculumVitaeDtoList == null) {
            return cvList;
        }
        for (CurriculumVitaeDto dto : curriculumVitaeDtoList) {
            cvList.add(mapCVDtoToCV(dto));
        }
        return cvList;
    }

    public static List<CurriculumVitaeDto> CollectorCvListToCvDtoList(List<Cv> cvList) {
        List<CurriculumVitaeDto> dtos = new ArrayList<>();
        if (cvList == null) {
            return dtos;
        }
        for (Cv cv : cvList) {
            dtos.add(mapCVToCVDto(cv));
        }
        return dtos;
    }
}
