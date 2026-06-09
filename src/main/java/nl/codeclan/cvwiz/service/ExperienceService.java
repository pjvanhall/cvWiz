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

    private final ExperienceRepository repo;

    public ExperienceService(ExperienceRepository repo) {
        this.repo = repo;
    }

    public ErvaringDto createExperience(@Nullable ErvaringDto dto) {
        ErvaringDto ex = new ErvaringDto();
        ex.setId(repo.count() + 1);
        if (dto != null) {
            dto.setId(ex.getId());
        } else {
            dto = ex;
        }
        return ExperienceMapper.ExperienceToExperienceDto(repo.save(ExperienceMapper.ExperienceDtoToExperience(dto)));
    }

    public List<ErvaringDto> createExperienceList(List<ErvaringDto> dtos) {
        List<ErvaringDto> list = new ArrayList<>();
        for (ErvaringDto ervaring : dtos) {
            list.add(createExperience(ervaring));
        }
        return list;
    }

    public void updateExperience(ErvaringDto ex) {
        repo.save(ExperienceMapper.ExperienceDtoToExperience(ex));
    }

    public void updateExperiencesList(List<ErvaringDto> dtos) {
        for (ErvaringDto dto : dtos) {
            updateExperience(dto);
        }
    }

    public void deleteExperience(ErvaringDto ex) throws EntityNotFoundException {
        if (repo.existsById(ex.getId())) {
            repo.deleteById(ex.getId());
        } else {
            throw new EntityNotFoundException("Deze ervaring bestaat niet in de database!");
        }
    }

}
