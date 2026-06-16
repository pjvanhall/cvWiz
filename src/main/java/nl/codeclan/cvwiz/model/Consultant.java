package nl.codeclan.cvwiz.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.util.List;
import java.util.UUID;

@Entity
public class Consultant {

    @Id
    UUID consultantId;
    String firstname;
    String lastname;
    String telephone;
    String email;
    @OneToOne
    Cv originalCV;
    @OneToMany
    List<Cv> usedCvs;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    CustomUser customUser;

    public Consultant() {
    }

    public Consultant(UUID consultantId, String firstname, String lastname, String telephone, String email, Cv originalCV, List<Cv> usedCvs) {
        this.consultantId = consultantId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.telephone = telephone;
        this.email = email;
        this.originalCV = originalCV;
        this.usedCvs = usedCvs;
    }

    public CustomUser getCustomUser() {
        return customUser;
    }

    public void setCustomUser(CustomUser customUser) {
        this.customUser = customUser;
    }

    public UUID getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(UUID consultantID) {
        this.consultantId = consultantID;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Cv getOriginalCV() {
        return originalCV;
    }

    public void setOriginalCV(Cv originalCV) {
        this.originalCV = originalCV;
    }

    public List<Cv> getUsedCvs() {
        return usedCvs;
    }

    public void setUsedCvs(List<Cv> usedCvs) {
        this.usedCvs = usedCvs;
    }


}
