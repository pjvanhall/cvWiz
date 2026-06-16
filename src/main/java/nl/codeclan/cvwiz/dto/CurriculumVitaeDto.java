package nl.codeclan.cvwiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CurriculumVitaeDto {
    Long id;
    @Size(max = 255)
    String bestandsNaam;
    @Size(max = 100)
    List<String> competenties;
    @NotNull
    @Size(max = 100)
    Map<String, String> languages;
    @Size(max = 20000000)
    String profiel;
    @Size(max = 20000000)
    String opleiding;
    @Valid
    TechniekMatrixDto matrix;
    @Valid
    @Size(max = 100)
    List<ErvaringDto> ervaring;

    public CurriculumVitaeDto() {
    }

    public CurriculumVitaeDto(Long id, String bestandsNaam, List<String> competenties, String profiel, String opleiding, TechniekMatrixDto matrix, List<ErvaringDto> ervaring) {
        this(id, bestandsNaam, competenties, new LinkedHashMap<>(), profiel, opleiding, matrix, ervaring);
    }

    public CurriculumVitaeDto(Long id, String bestandsNaam, List<String> competenties, Map<String, String> languages, String profiel, String opleiding, TechniekMatrixDto matrix, List<ErvaringDto> ervaring) {
        this.id = id;
        this.bestandsNaam = bestandsNaam;
        this.competenties = competenties;
        this.languages = languages == null ? new LinkedHashMap<>() : new LinkedHashMap<>(languages);
        this.profiel = profiel;
        this.opleiding = opleiding;
        this.matrix = matrix;
        this.ervaring = ervaring;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBestandsNaam() {
        return bestandsNaam;
    }

    public void setBestandsNaam(String bestandsNaam) {
        this.bestandsNaam = bestandsNaam;
    }

    public List<String> getCompetenties() {
        return competenties;
    }

    public void setCompetenties(List<String> competenties) {
        this.competenties = competenties;
    }

    public Map<String, String> getLanguages() {
        return languages;
    }

    public void setLanguages(Map<String, String> languages) {
        this.languages = languages;
    }

    public String getProfiel() {
        return profiel;
    }

    public void setProfiel(String profiel) {
        this.profiel = profiel;
    }

    public String getOpleiding() {
        return opleiding;
    }

    public void setOpleiding(String opleiding) {
        this.opleiding = opleiding;
    }

    public TechniekMatrixDto getMatrix() {
        return matrix;
    }

    public void setMatrix(TechniekMatrixDto matrix) {
        this.matrix = matrix;
    }

    public List<ErvaringDto> getErvaring() {
        return ervaring;
    }

    public void setErvaring(List<ErvaringDto> ervaring) {
        this.ervaring = ervaring;
    }
}
