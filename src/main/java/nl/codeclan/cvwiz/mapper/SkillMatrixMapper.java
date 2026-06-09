package nl.codeclan.cvwiz.mapper;


import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.model.SkillMatrix;

public class SkillMatrixMapper {

    public static TechniekMatrixDto mapSkillMatrixToDto(SkillMatrix s) {
        return new TechniekMatrixDto(s.getSkillMatrixId(), s.getSkills());
    }

    public static SkillMatrix mapDtoToSkillMatrix(TechniekMatrixDto dto) {
        return new SkillMatrix(dto.id(), dto.matrix());
    }
}
