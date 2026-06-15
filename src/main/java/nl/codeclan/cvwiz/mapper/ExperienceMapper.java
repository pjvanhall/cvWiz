package nl.codeclan.cvwiz.mapper;


import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.model.Experience;

import java.util.ArrayList;
import java.util.List;

public class ExperienceMapper {

    public static Experience ExperienceDtoToExperience(ErvaringDto dto) {
        if (dto == null) {
            return null;
        }
        return new Experience(dto.getId(), dto.getBedrijf(), dto.getPeriode(), dto.getFunctie(), dto.getSector(), dto.getKennis(), dto.getSituatie(), dto.getTaak());
    }

    // mapper to map Experience to ExperienceDto
    public static ErvaringDto ExperienceToExperienceDto(Experience e) {
        if (e == null) {
            return null;
        }
        return new ErvaringDto(e.getId(), e.getCompany(), e.getPeriod(), e.getJobTitle(), e.getBranche(), e.getTechStack(), e.getSituation(), e.getTask());
    }

    // collector for Experience List
    public static List<Experience> CollectorExperienceDtoListToExperienceList(List<ErvaringDto> ervaringDtoList) {
        List<Experience> experiences = new ArrayList<>();
        if (ervaringDtoList == null) {
            return experiences;
        }
        for (ErvaringDto e : ervaringDtoList) {
            Experience experience = ExperienceDtoToExperience(e);
            if (experience != null) {
                experiences.add(experience);
            }
        }
        return experiences;
    }

    // collector for ExperienceDto List
    public static List<ErvaringDto> CollectorExperienceListToExperienceDtoList(List<Experience> experienceList) {
        List<ErvaringDto> experiences = new ArrayList<>();
        if (experienceList == null) {
            return experiences;
        }
        for (Experience e : experienceList) {
            ErvaringDto ervaringDto = ExperienceToExperienceDto(e);
            if (ervaringDto != null) {
                experiences.add(ervaringDto);
            }
        }
        return experiences;
    }
}
