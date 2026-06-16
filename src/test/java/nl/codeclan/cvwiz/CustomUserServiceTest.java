package nl.codeclan.cvwiz;

import nl.codeclan.cvwiz.model.Authorisatie;
import nl.codeclan.cvwiz.model.CustomUser;
import nl.codeclan.cvwiz.repository.CustomUserRepository;
import nl.codeclan.cvwiz.service.CustomUserDetailService;
import nl.codeclan.cvwiz.service.CustomUserService;
import nl.codeclan.cvwiz.service.GeneratedCustomUser;
import nl.codeclan.cvwiz.support.RepositoryDoubles;
import nl.codeclan.cvwiz.support.TestData;
import nl.codeclan.cvwiz.support.TestPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomUserServiceTest {

    private RepositoryDoubles.TestRepository<CustomUserRepository, CustomUser, String> repository;
    private CustomUserService service;

    @BeforeEach
    void setUp() {
        repository = RepositoryDoubles.users();
        service = new CustomUserService(repository.repository(), new TestPasswordEncoder());
    }

    @Test
    void getsUsersByListAndId() {
        CustomUser manager = TestData.user("manager", "ROLE_MANAGER");
        CustomUser consultant = TestData.user("consultant", "ROLE_CONSULTANT");
        repository.put(manager);
        repository.put(consultant);

        List<CustomUser> users = service.getUsers();

        assertThat(users).containsExactly(manager, consultant);
        assertThat(service.getUserById("manager")).isSameAs(manager);
        assertThat(service.existsByUsername("consultant")).isTrue();
        assertThat(service.existsByUsername("missing")).isFalse();
    }

    @Test
    void getUserByIdThrowsWhenMissing() {
        assertThrows(UsernameNotFoundException.class, () -> service.getUserById("missing"));
    }

    @Test
    void createsGeneratedManagerAndConsultantUsersWithoutPersistingThem() {
        GeneratedCustomUser manager = service.createGeneratedCustomUserWithPassword("manager", "manager@example.com", "Beheerder");
        CustomUser consultant = service.createGeneratedCustomUser("consultant", "consultant@example.com", "ROLE_CONSULTANT");

        assertThat(manager.user().getPassword()).startsWith("encoded:");
        assertThat(manager.oneTimePassword()).hasSize(20);
        assertThat(manager.user().getAuthorisaties()).extracting(Authorisatie::getAuthorisatie).containsExactly("ROLE_MANAGER");
        assertThat(consultant.getAuthorisaties()).extracting(Authorisatie::getAuthorisatie).containsExactly("ROLE_CONSULTANT");
        assertThat(repository.savedEntities()).isEmpty();
    }

    @Test
    void generatedUserRejectsUnknownRoles() {
        assertThrows(IllegalArgumentException.class, () -> service.createGeneratedCustomUser("user", "user@example.com", null));
        assertThrows(IllegalArgumentException.class, () -> service.createGeneratedCustomUser("user", "user@example.com", "ROLE_ADMIN"));
    }

    @Test
    void disablesUser() {
        CustomUser user = TestData.user("consultant", "ROLE_CONSULTANT");
        repository.put(user);

        service.disableUser("consultant");

        assertThat(user.isEnabled()).isFalse();
        assertThat(repository.savedEntities()).contains(user);
    }

    @Test
    void addsAndRemovesAuthoritiesOnlyWhenUserExists() {
        CustomUser user = new CustomUser("manager", "manager@example.com", "encoded:password", true);
        user.setAuthorisaties(Set.of(new Authorisatie("manager", "ROLE_MANAGER")));
        repository.put(user);

        service.addAuthority("manager", "ROLE_CONSULTANT");
        service.addAuthority("missing", "ROLE_MANAGER");
        service.removeAuthority("manager", "ROLE_MANAGER");
        service.removeAuthority("missing", "ROLE_MANAGER");

        assertThat(user.getAuthorisaties()).extracting(Authorisatie::getAuthorisatie).containsExactly("ROLE_CONSULTANT");
        assertThat(repository.savedEntities()).hasSize(2);
    }

    @Test
    void userDetailsServiceMapsEnabledStatePasswordAndAuthorities() {
        CustomUser user = TestData.user("manager", "ROLE_MANAGER");
        repository.put(user);
        CustomUserDetailService detailService = new CustomUserDetailService(service);

        UserDetails details = detailService.loadUserByUsername("manager");

        assertThat(details.getUsername()).isEqualTo("manager");
        assertThat(details.getPassword()).isEqualTo("encoded:password");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_MANAGER");
    }
}
