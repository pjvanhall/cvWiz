package nl.codeclan.cvwiz.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.util.Objects;

@Entity
public class Authorisatie implements Serializable {
    @Id
    private String username;
    @Id
    private String authorisatie;

    public Authorisatie() {
    }

    public Authorisatie(String username, String authorisatie) {
        this.username = username;
        this.authorisatie = authorisatie;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthorisatie() {
        return authorisatie;
    }

    public void setAuthorisatie(String authorisatie) {
        this.authorisatie = authorisatie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Authorisatie that)) {
            return false;
        }
        return Objects.equals(username, that.username)
                && Objects.equals(authorisatie, that.authorisatie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, authorisatie);
    }
}
