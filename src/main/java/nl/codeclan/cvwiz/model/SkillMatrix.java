package nl.codeclan.cvwiz.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
public class SkillMatrix {

    @Id
    Long skillMatrixId;
    // one skill contains a list of category's with each a list of technics and scores
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Map<String, Map<String, Integer>> skills;

    public SkillMatrix() {
    }

    public SkillMatrix(Long skillMatrixId, Map<String, Map<String, Integer>> skills) {
        this.skillMatrixId = skillMatrixId;
        this.skills = skills;
    }

    public Long getSkillMatrixId() {
        return skillMatrixId;
    }

    public void setSkillMatrixId(Long skillMatrixId) {
        this.skillMatrixId = skillMatrixId;
    }

    public Map<String, Map<String, Integer>> getSkills() {
        return skills;
    }

    public void setSkills(Map<String, Map<String, Integer>> skills) {
        this.skills = skills;
    }
}