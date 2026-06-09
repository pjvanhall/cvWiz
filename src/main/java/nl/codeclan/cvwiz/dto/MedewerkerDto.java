package nl.codeclan.cvwiz.dto;

import java.util.List;

public class MedewerkerDto {
    String id;
    final String voornaam;
    final String achternaam;
    final String telefoon;
    final String emailAdres;
    CurriculumVitaeDto orgineleCv;
    List<CurriculumVitaeDto> cvLijst;

    public MedewerkerDto(String voornaam, String id, String achternaam, String telefoon, String emailAdres, CurriculumVitaeDto orgineleCv, List<CurriculumVitaeDto> cvLijst) {
        this.voornaam = voornaam;
        this.id = id;
        this.achternaam = achternaam;
        this.telefoon = telefoon;
        this.emailAdres = emailAdres;
        this.orgineleCv = orgineleCv;
        this.cvLijst = cvLijst;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVoornaam() {
        return voornaam;
    }


    public String getAchternaam() {
        return achternaam;
    }

    public String getTelefoon() {
        return telefoon;
    }

    public String getEmailAdres() {
        return emailAdres;
    }

    public CurriculumVitaeDto getOrgineleCv() {
        return orgineleCv;
    }

    public void setOrgineleCv(CurriculumVitaeDto orgineleCv) {
        this.orgineleCv = orgineleCv;
    }

    public List<CurriculumVitaeDto> getCvLijst() {
        return cvLijst;
    }

    public void setCvLijst(List<CurriculumVitaeDto> cvLijst) {
        this.cvLijst = cvLijst;
    }
}