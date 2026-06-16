package nl.codeclan.cvwiz.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Cv {

    @Id
    Long cvId;
    String fileName;
    @ElementCollection
    @CollectionTable(name = "cv_tech_stack", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "technology")
    List<String> techStack = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "cv_languages", joinColumns = @JoinColumn(name = "cv_id"))
    @MapKeyColumn(name = "language")
    @Column(name = "niveau")
    Map<String, String> languages = new LinkedHashMap<>();
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
        this(cvId, fileName, techStack, new LinkedHashMap<>(), profile, education, skillMatrix, experience);
    }

    public Cv(Long cvId, String fileName, List<String> techStack, Map<String, String> languages, String profile, String education, SkillMatrix skillMatrix, List<Experience> experience) {
        this.cvId = cvId;
        this.fileName = fileName;
        this.techStack = techStack;
        this.languages = languages == null ? new LinkedHashMap<>() : new LinkedHashMap<>(languages);
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

    public Map<String, String> getLanguages() {
        return languages;
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
