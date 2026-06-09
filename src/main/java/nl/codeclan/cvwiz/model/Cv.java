package nl.codeclan.cvwiz.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Cv {

    @Id
    Long cvId;
    String fileName;
    @ElementCollection
    @CollectionTable(name = "cv_tech_stack", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "technology")
    List<String> techStack = new ArrayList<>();
    @Column(columnDefinition = "TEXT")
    String profile;
    @Column(columnDefinition = "TEXT")
    String education;
    @ManyToOne(fetch = FetchType.LAZY)
    SkillMatrix skillMatrix;
    @OneToMany
    List<Experience> experience;

    public Cv() {
    }

    public Cv(Long cvId, String fileName, List<String> techStack, String profile, String education, SkillMatrix skillMatrix, List<Experience> experience) {
        this.cvId = cvId;
        this.fileName = fileName;
        this.techStack = techStack;
        this.profile = profile;
        this.education = education;
        this.skillMatrix = skillMatrix;
        this.experience = experience;
    }

    public Long getCvId() {
        return cvId;
    }

    public String getFileName() {
        return fileName;
    }

    public List<String> getTechStack() {
        return techStack;
    }

    public String getProfile() {
        return profile;
    }

    public String getEducation() {
        return education;
    }

    public SkillMatrix getSkillMatrix() {
        return skillMatrix;
    }

    public List<Experience> getExperience() {
        return experience;
    }
}