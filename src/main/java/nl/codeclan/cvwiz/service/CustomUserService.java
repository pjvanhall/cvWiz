package nl.codeclan.cvwiz.service;

import nl.codeclan.cvwiz.model.Authorisatie;
import nl.codeclan.cvwiz.model.CustomUser;
import nl.codeclan.cvwiz.repository.CustomUserRepository;
import nl.codeclan.cvwiz.util.InputValidationUtil;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserService {
    private final CustomUserRepository customUserRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserService(CustomUserRepository customUserRepository, PasswordEncoder passwordEncoder) {
        this.customUserRepository = customUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<CustomUser> getUsers() {
        return customUserRepository.findAll();
    }

    public CustomUser getUserById(String username) throws UsernameNotFoundException {
        Optional<CustomUser> customUser = customUserRepository.findById(username);
        if (customUser.isEmpty()) {
            throw new UsernameNotFoundException(username);
        } else {
            return customUser.get();
        }
    }

    public CustomUser getUserByEmail(String email) throws UsernameNotFoundException {
        Optional<CustomUser> customUser = customUserRepository.findByEmailIgnoreCase(email);
        if (customUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        } else {
            return customUser.get();
        }
    }

    public boolean existsByUsername(String username) {
        return customUserRepository.existsById(username);
    }

    public CustomUser createGeneratedCustomUser(String username, String email, String role) {
        return createGeneratedCustomUserWithPassword(username, email, role).user();
    }

    public GeneratedCustomUser createGeneratedCustomUserWithPassword(String username, String email, String role) {
        String authority = getAuthorityForRole(role);
        if (authority == null) {
            throw new IllegalArgumentException("Unknown role: " + role);
        }

        String generatedPassword = InputValidationUtil.generateSafePassword(20);
        CustomUser customUser = new CustomUser(username, email, passwordEncoder.encode(generatedPassword), true);
        customUser.addAuthorisatie(new Authorisatie(username, authority));
        return new GeneratedCustomUser(customUser, generatedPassword);
    }

    public void disableUser(String username) {
        CustomUser customUser = getUserById(username);
        customUser.setEnabled(false);
        customUserRepository.save(customUser);
    }

    private String getAuthorityForRole(String role) {
        if (role == null) {
            return null;
        }
        if (role.equalsIgnoreCase("Beheerder") || role.equalsIgnoreCase("ROLE_MANAGER")) {
            return "ROLE_MANAGER";
        }
        if (role.equalsIgnoreCase("Medewerker") || role.equalsIgnoreCase("ROLE_CONSULTANT")) {
            return "ROLE_CONSULTANT";
        }
        return null;
    }

    public void addAuthority(String username, String authority) {
        if (existsByUsername(username)) {
            CustomUser custom = customUserRepository.findById(username).get();
            custom.addAuthorisatie(new Authorisatie(username, authority));
            customUserRepository.save(custom);
        }
    }

    public void removeAuthority(String username, String authority) {
        if (existsByUsername(username)) {
            CustomUser custom = customUserRepository.findById(username).get();
            Authorisatie auth = custom.getAuthorisaties().stream().filter((a) -> a.getAuthorisatie().equalsIgnoreCase(authority)).findAny().get();
            custom.removeAuthorisatie(auth);
            customUserRepository.save(custom);
        }
    }
}
