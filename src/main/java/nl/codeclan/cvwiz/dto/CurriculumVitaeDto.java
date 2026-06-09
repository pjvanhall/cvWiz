package nl.codeclan.cvwiz.dto;

import java.util.List;

public class CurriculumVitaeDto {
    Long id;
    String bestandsNaam;
    List<String> competenties;
    String profiel;
    String opleiding;
    TechniekMatrixDto matrix;
    List<ErvaringDto> ervaring;

    public CurriculumVitaeDto() {
    }

    public CurriculumVitaeDto(Long id, String bestandsNaam, List<String> competenties, String profiel, String opleiding, TechniekMatrixDto matrix, List<ErvaringDto> ervaring) {
        this.id = id;
        this.bestandsNaam = bestandsNaam;
        this.competenties = competenties;
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
