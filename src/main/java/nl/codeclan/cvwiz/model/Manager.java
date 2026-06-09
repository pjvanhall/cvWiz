package nl.codeclan.cvwiz.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Manager {

    @Id
    UUID managerId;
    String firstname;
    String lastname;
    String telephone;
    String email;

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
