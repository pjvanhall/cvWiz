package nl.codeclan.cvwiz;

import nl.codeclan.cvwiz.dto.BeheerderDto;
import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.mapper.CVMapper;
import nl.codeclan.cvwiz.mapper.ConsultantMapper;
import nl.codeclan.cvwiz.mapper.ExperienceMapper;
import nl.codeclan.cvwiz.mapper.ManagerMapper;
import nl.codeclan.cvwiz.mapper.SkillMatrixMapper;
import nl.codeclan.cvwiz.model.Consultant;
import nl.codeclan.cvwiz.model.Cv;
import nl.codeclan.cvwiz.model.Experience;
import nl.codeclan.cvwiz.model.Manager;
import nl.codeclan.cvwiz.model.SkillMatrix;
import nl.codeclan.cvwiz.support.TestData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    @Test
    void mapperClassesCanBeConstructed() {
        assertThat(new SkillMatrixMapper()).isNotNull();
        assertThat(new ManagerMapper()).isNotNull();
        assertThat(new ExperienceMapper()).isNotNull();
        assertThat(new CVMapper()).isNotNull();
        assertThat(new ConsultantMapper()).isNotNull();
    }

    @Test
    void mapsSkillMatricesBothWays() {
        SkillMatrix matrix = TestData.skillMatrix(5L, "Backend", "Java");

        TechniekMatrixDto dto = SkillMatrixMapper.mapSkillMatrixToDto(matrix);
        SkillMatrix mapped = SkillMatrixMapper.mapDtoToSkillMatrix(dto);

        assertThat(SkillMatrixMapper.mapSkillMatrixToDto(null)).isNull();
        assertThat(SkillMatrixMapper.mapDtoToSkillMatrix(null)).isNull();
        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.matrix()).containsKey("Backend");
        assertThat(mapped.getSkillMatrixId()).isEqualTo(5L);
        assertThat(mapped.getSkills()).isEqualTo(matrix.getSkills());
    }

    @Test
    void mapsManagersWithAndWithoutIds() {
        BeheerderDto dtoWithId = TestData.managerDto(TestData.MANAGER_ID);
        BeheerderDto dtoWithoutId = new BeheerderDto("John", null, "Manager", "06", "john@example.com");

        Manager managerWithId = ManagerMapper.managerDtoToManager(dtoWithId);
        Manager managerWithoutId = ManagerMapper.managerDtoToManager(dtoWithoutId);
        BeheerderDto mappedBack = ManagerMapper.managerToManagerDto(managerWithId);

        assertThat(managerWithId.getManagerId()).isEqualTo(TestData.MANAGER_ID);
        assertThat(managerWithoutId.getManagerId()).isNull();
        assertThat(mappedBack.getVoornaam()).isEqualTo("John");
        assertThat(mappedBack.getId()).isEqualTo(TestData.MANAGER_ID.toString());
        assertThat(mappedBack.getTelefoon()).isEqualTo("0698765432");
        assertThat(mappedBack.getEmailAdres()).isEqualTo("john.manager@example.com");
    }

    @Test
    void mapsExperiencesAndListsBothWays() {
        ErvaringDto dto = TestData.experienceDto(7L);

        Experience experience = ExperienceMapper.ExperienceDtoToExperience(dto);
        ErvaringDto mappedDto = ExperienceMapper.ExperienceToExperienceDto(experience);
        List<Experience> experiences = ExperienceMapper.CollectorExperienceDtoListToExperienceList(List.of(dto));
        List<ErvaringDto> dtos = ExperienceMapper.CollectorExperienceListToExperienceDtoList(List.of(experience));

        assertThat(ExperienceMapper.ExperienceDtoToExperience(null)).isNull();
        assertThat(ExperienceMapper.ExperienceToExperienceDto(null)).isNull();
        assertThat(ExperienceMapper.CollectorExperienceDtoListToExperienceList(null)).isEmpty();
        assertThat(ExperienceMapper.CollectorExperienceListToExperienceDtoList(null)).isEmpty();
        assertThat(experience.getId()).isEqualTo(7L);
        assertThat(mappedDto.getBedrijf()).isEqualTo("CodeClan");
        assertThat(experiences).hasSize(1);
        assertThat(dtos).hasSize(1);
    }

    @Test
    void mapsCvsAndHandlesNullLists() {
        CurriculumVitaeDto dto = TestData.cvDto(3L);

        Cv cv = CVMapper.mapCVDtoToCV(dto);
        CurriculumVitaeDto mappedDto = CVMapper.mapCVToCVDto(cv);

        assertThat(CVMapper.mapCVDtoToCV(null)).isNull();
        assertThat(CVMapper.mapCVToCVDto(null)).isNull();
        assertThat(CVMapper.CollectorCvDtoListToCvList(null)).isEmpty();
        assertThat(CVMapper.CollectorCvListToCvDtoList(null)).isEmpty();
        assertThat(CVMapper.CollectorCvDtoListToCvList(List.of(dto))).hasSize(1);
        assertThat(CVMapper.CollectorCvListToCvDtoList(List.of(cv))).hasSize(1);
        assertThat(cv.getLanguages()).containsEntry("Dutch", "Native").containsEntry("English", "Professional");
        assertThat(mappedDto.getId()).isEqualTo(3L);
        assertThat(mappedDto.getLanguages()).containsEntry("Dutch", "Native").containsEntry("English", "Professional");
        assertThat(mappedDto.getMatrix().matrix()).containsKey("Backend");
    }

    @Test
    void mapsConsultantsBothWays() {
        UUID id = TestData.CONSULTANT_ID;
        CurriculumVitaeDto originalCv = TestData.cvDto(1L);
        CurriculumVitaeDto usedCv = TestData.cvDto(2L);
        MedewerkerDto dto = TestData.consultantDto(id, originalCv, List.of(usedCv));

        Consultant consultant = ConsultantMapper.mapConsultantDtoToConsultant(dto);
        MedewerkerDto mappedDto = ConsultantMapper.mapConsultantToConsultantDto(consultant);

        assertThat(consultant.getConsultantId()).isEqualTo(id);
        assertThat(consultant.getOriginalCV().getCvId()).isEqualTo(1L);
        assertThat(mappedDto.getId()).isEqualTo(id.toString());
        assertThat(mappedDto.getCvLijst()).hasSize(1);
    }
}
