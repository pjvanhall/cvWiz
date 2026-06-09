package nl.codeclan.cvwiz;

import jakarta.persistence.EntityNotFoundException;

import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.mapper.ExperienceMapper;
import nl.codeclan.cvwiz.model.Experience;
import nl.codeclan.cvwiz.repository.ExperienceRepository;
import nl.codeclan.cvwiz.service.ExperienceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExperienceServiceTest {

    @InjectMocks
    private ExperienceService service;

    @Mock
    private ExperienceRepository repo;
    @Mock
    private ErvaringDto dto;
    @Mock
    private ErvaringDto dto1;
    @Mock
    private Experience exp;


    @BeforeEach
    public void init() {
        dto = new ErvaringDto(1L, "bedrijf.bv", "30-10-2021 t/m 30-04-2026", "directeur", "branche", "heel veel", "gevaarlijk", "overleven");
        dto1 = new ErvaringDto(2L, "ander bedrijf.bv", "30-10-2021 t/m 30-04-2026", "concierge", "schoonmaak", "heel veel poetsen", "vies", "schoonmaken");
        exp = new Experience(1L, "bedrijf.bv", "30-10-2021 t/m 30-04-2026", "directeur", "branche", "heel veel", "gevaarlijk", "overleven");
       }

    @Test
    public void createExperienceTestWithDtoNull() {
        when(repo.count()).thenReturn(0L);
        when(repo.save(any())).thenReturn(exp);
        MockedStatic<ExperienceMapper> mockedStatic = Mockito.mockStatic(ExperienceMapper.class);
        mockedStatic.when(() -> ExperienceMapper.ExperienceToExperienceDto(any())).thenReturn(dto);
        mockedStatic.when(() -> ExperienceMapper.ExperienceDtoToExperience(any())).thenReturn(exp);

        dto1 = service.createExperience(null);

        verify(repo,times(1)).count();
        verify(repo,times(1)).save(any());
        assertThat(dto.getId()).isEqualTo(1L);
        mockedStatic.close();
    }

    @Test
    public void createExperienceTestWithDto() {
        when(repo.count()).thenReturn(0L);
        when(repo.save(any())).thenReturn(exp);
        MockedStatic<ExperienceMapper> mockedStatic = Mockito.mockStatic(ExperienceMapper.class);
        mockedStatic.when(() -> ExperienceMapper.ExperienceToExperienceDto(any())).thenReturn(dto);
        mockedStatic.when(() -> ExperienceMapper.ExperienceDtoToExperience(any())).thenReturn(exp);

        dto1 = service.createExperience(dto);

        verify(repo,times(1)).count();
        verify(repo,times(1)).save(any());
        assertThat(dto.getId()).isEqualTo(1L);
        mockedStatic.close();
    }

    @Test
    public void createExperienceListTest() {
        when(repo.count()).thenReturn(0L);
        when(repo.save(any())).thenReturn(exp);
        MockedStatic<ExperienceMapper> mockedStatic = Mockito.mockStatic(ExperienceMapper.class);
        mockedStatic.when(() -> ExperienceMapper.ExperienceToExperienceDto(any())).thenReturn(dto);
        mockedStatic.when(() -> ExperienceMapper.ExperienceDtoToExperience(any())).thenReturn(exp);

        List<ErvaringDto> list = service.createExperienceList(List.of(dto, dto1));

        verify(repo,times(2)).save(any());
        assertThat(list.size()).isEqualTo(2);
        mockedStatic.close();
    }
    @Test
    public void updateExperienceTest() {
        service.updateExperience(dto);
        verify(repo,times(1)).save(any());
    }

    @Test
    public void updateExperienceList() {
        List<ErvaringDto> ervaringDtos = new ArrayList<>();
        ervaringDtos.add(dto);
        ervaringDtos.add(dto1);
        service.updateExperiencesList(ervaringDtos);
        verify(repo,times(2)).save(any());
    }

    @Test
    public void deleteExperienceTest() {
        when(repo.existsById(1L)).thenReturn(true);
        service.deleteExperience(dto);
        verify(repo,times(1)).deleteById(1L);
    }

    @Test
    public void deleteExperienceThrowsEntityNotFoundExceptionTest() {
        when(repo.existsById(1L)).thenReturn(false);
        assertThrowsExactly(EntityNotFoundException.class, ()-> service.deleteExperience(dto));
    }
}
