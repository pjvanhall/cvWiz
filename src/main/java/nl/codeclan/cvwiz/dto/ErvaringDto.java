package nl.codeclan.cvwiz.dto;

import jakarta.validation.constraints.Size;

public class ErvaringDto {
    Long id;
    @Size(max = 255)
    String bedrijf;
    @Size(max = 100)
    String periode;
    @Size(max = 255)
    String functie;
    @Size(max = 255)
    String sector;
    @Size(max = 20000)
    String kennis;
    @Size(max = 20000)
    String situatie;
    @Size(max = 20000)
    String taak;

    public ErvaringDto() {
    }

    public ErvaringDto(Long id, String bedrijf, String periode, String functie, String sector, String kennis, String situatie, String taak) {
        this.id = id;
        this.bedrijf = bedrijf;
        this.periode = periode;
        this.functie = functie;
        this.sector = sector;
        this.kennis = kennis;
        this.situatie = situatie;
        this.taak = taak;
    }

    public String getSituatie() {
        return situatie;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBedrijf() {
        return bedrijf;
    }

    public void setBedrijf(String bedrijf) {
        this.bedrijf = bedrijf;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getFunctie() {
        return functie;
    }

    public void setFunctie(String functie) {
        this.functie = functie;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getKennis() {
        return kennis;
    }

    public void setKennis(String kennis) {
        this.kennis = kennis;
    }

    public void setSituatie(String situatie) {
        this.situatie = situatie;
    }

    public String getTaak() {
        return taak;
    }

    public void setTaak(String taak) {
        this.taak = taak;
    }
}
