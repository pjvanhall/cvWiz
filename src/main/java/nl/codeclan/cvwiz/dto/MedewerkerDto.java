package nl.codeclan.cvwiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class MedewerkerDto {
    @Size(max = 36)
    String id;
    @NotBlank
    @Size(max = 100)
    final String voornaam;
    @NotBlank
    @Size(max = 100)
    final String achternaam;
    @Size(max = 50)
    final String telefoon;
    @NotBlank
    @Email
    @Size(max = 254)
    final String emailAdres;
    @Valid
    CurriculumVitaeDto orgineleCv;
    @Valid
    @Size(max = 50)
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
