package nl.codeclan.cvwiz.mapper;


import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.model.SkillMatrix;

public class SkillMatrixMapper {

    public static TechniekMatrixDto mapSkillMatrixToDto(SkillMatrix s) {
        if (s == null) {
            return null;
        }
        return new TechniekMatrixDto(s.getSkillMatrixId(), s.getSkills());
    }

    public static SkillMatrix mapDtoToSkillMatrix(TechniekMatrixDto dto) {
        if (dto == null) {
            return null;
        }
        return new SkillMatrix(dto.id(), dto.matrix());
    }
}
