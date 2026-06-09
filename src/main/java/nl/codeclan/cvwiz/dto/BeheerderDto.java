package nl.codeclan.cvwiz.dto;

public class BeheerderDto {
    String id;
    final String voornaam;
    final String achternaam;
    final String telefoon;
    final String emailAdres;

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

}
