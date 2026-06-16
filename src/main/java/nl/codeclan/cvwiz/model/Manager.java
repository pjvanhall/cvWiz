package nl.codeclan.cvwiz.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import java.util.UUID;

@Entity
public class Manager {

    @Id
    UUID managerId;
    String firstname;
    String lastname;
    String telephone;
    String email;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    CustomUser customUser;

    public Manager() {
    }

    public Manager(String firstname, String lastname, String telephone, String email) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.telephone = telephone;
        this.email = email;
    }

    public Manager(UUID managerId, String firstname, String lastname, String telephone, String email) {
        this.managerId = managerId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.telephone = telephone;
        this.email = email;
    }

    public Manager(UUID managerId, String firstname, String lastname, String telephone, String email, CustomUser customUser) {
        this.managerId = managerId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.telephone = telephone;
        this.email = email;
        this.customUser = customUser;
    }

    public CustomUser getCustomUser() {
        return customUser;
    }

    public void setCustomUser(CustomUser customUser) {
        this.customUser = customUser;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }
}
