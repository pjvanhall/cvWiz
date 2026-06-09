package nl.codeclan.cvwiz.mapper;


import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.model.Experience;

import java.util.ArrayList;
import java.util.List;

public class ExperienceMapper {

    public static Experience ExperienceDtoToExperience(ErvaringDto dto) {
        return new Experience(dto.getId(), dto.getBedrijf(), dto.getPeriode(), dto.getFunctie(), dto.getSector(), dto.getKennis(), dto.getSituatie(), dto.getTaak());
    }

    // mapper to map Experience to ExperienceDto
    public static ErvaringDto ExperienceToExperienceDto(Experience e) {
        return new ErvaringDto(e.getId(), e.getCompany(), e.getPeriod(), e.getJobTitle(), e.getBranche(), e.getTechStack(), e.getSituation(), e.getTask());
    }

    // collector for Experience List
    public static List<Experience> CollectorExperienceDtoListToExperienceList(List<ErvaringDto> ervaringDtoList) {
        List<Experience> experiences = new ArrayList<>();
        for (ErvaringDto e : ervaringDtoList) {
            experiences.add(ExperienceDtoToExperience(e));
        }
        return experiences;
    }

    // collector for ExperienceDto List
    public static List<ErvaringDto> CollectorExperienceListToExperienceDtoList(List<Experience> experienceList) {
        List<ErvaringDto> experiences = new ArrayList<>();
        for (Experience e : experienceList) {
            experiences.add(ExperienceToExperienceDto(e));
        }
        return experiences;
    }
}
