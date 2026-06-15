package nl.codeclan.cvwiz.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.HashSet;
import java.util.Set;

@Entity
public class CustomUser {

    @Id
    String username;
    String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password;
    boolean enabled;

    @OneToMany(targetEntity = Authorisatie.class, mappedBy = "username", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    Set<Authorisatie> authorisaties =  new HashSet<>();

    public CustomUser() {
    }

    public CustomUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public CustomUser(String username, String email, String password,  boolean enabled ) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Authorisatie> getAuthorisaties() {
        return authorisaties;
    }

    public void addAuthorisatie(Authorisatie authorisatie){
        this.authorisaties.add(authorisatie);
    }

    public void removeAuthorisatie(Authorisatie authorisatie){
        this.authorisaties.remove(authorisatie);
    }

    public void setAuthorisaties(Set<Authorisatie> authorisaties) {
        this.authorisaties = authorisaties == null ? new HashSet<>() : new HashSet<>(authorisaties);
    }
}
