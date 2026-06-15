package nl.codeclan.cvwiz.service;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.mapper.ExperienceMapper;
import nl.codeclan.cvwiz.repository.ExperienceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    public ExperienceService(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
    }

    public ErvaringDto createExperience(@Nullable ErvaringDto dto) {
        ErvaringDto experience = dto == null ? new ErvaringDto() : dto;
        experience.setId(nextExperienceId());
        return saveExperience(experience);
    }

    public List<ErvaringDto> createExperienceList(List<ErvaringDto> dtos) {
        List<ErvaringDto> experiences = new ArrayList<>();
        if (dtos == null) {
            return experiences;
        }
        for (ErvaringDto ervaringDto : dtos) {
            experiences.add(createExperience(ervaringDto));
        }
        return experiences;
    }

    public ErvaringDto updateExperience(ErvaringDto dto) {
        if (dto == null) {
            return null;
        }
        return saveExperience(dto);
    }

    public ErvaringDto saveSubmittedExperience(ErvaringDto dto) {
        if (dto == null) {
            return null;
        }
        if (dto.getId() == null) {
            return createExperience(dto);
        }
        return updateExperience(dto);
    }

    public List<ErvaringDto> saveSubmittedExperiencesList(List<ErvaringDto> dtos) {
        List<ErvaringDto> experiences = new ArrayList<>();
        if (dtos == null) {
            return experiences;
        }
        for (ErvaringDto dto : dtos) {
            ErvaringDto savedExperience = saveSubmittedExperience(dto);
            if (savedExperience != null) {
                experiences.add(savedExperience);
            }
        }
        return experiences;
    }

    public void updateExperiencesList(List<ErvaringDto> dtos) {
        if (dtos == null) {
            return;
        }
        for (ErvaringDto dto : dtos) {
            updateExperience(dto);
        }
    }

    public void deleteExperience(ErvaringDto dto) throws EntityNotFoundException {
        if (experienceRepository.existsById(dto.getId())) {
            experienceRepository.deleteById(dto.getId());
        } else {
            throw new EntityNotFoundException("Deze ervaring bestaat niet in de database!");
        }
    }

    private ErvaringDto saveExperience(ErvaringDto dto) {
        return ExperienceMapper.ExperienceToExperienceDto(experienceRepository.save(ExperienceMapper.ExperienceDtoToExperience(dto)));
    }

    private Long nextExperienceId() {
        long id = experienceRepository.count() + 1;
        while (experienceRepository.existsById(id)) {
            id++;
        }
        return id;
    }
}
