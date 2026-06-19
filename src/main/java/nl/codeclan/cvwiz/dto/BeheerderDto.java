package nl.codeclan.cvwiz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BeheerderDto {
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
    boolean hasCv;

    public BeheerderDto(String voornaam, String id, String achternaam, String telefoon, String emailAdres) {
        this.voornaam = voornaam;
        this.id = id;
        this.achternaam = achternaam;
        this.telefoon = telefoon;
        this.emailAdres = emailAdres;
    }

    public String getVoornaam() {
        return voornaam;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isHasCv() {
        return hasCv;
    }

    public void setHasCv(boolean hasCv) {
        this.hasCv = hasCv;
    }

}
