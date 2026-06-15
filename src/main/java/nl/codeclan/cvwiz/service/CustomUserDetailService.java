package nl.codeclan.cvwiz.service;

import nl.codeclan.cvwiz.model.CustomUser;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final CustomUserService customUserService;

    public CustomUserDetailService(CustomUserService customUserService) {
        this.customUserService = customUserService;
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) {
        CustomUser customUser = customUserService.getUserById(username);
        List<GrantedAuthority> grantedAuthorities = customUser.getAuthorisaties()
                .stream()
                .map(authorisatie -> new SimpleGrantedAuthority(authorisatie.getAuthorisatie()))
                .map(GrantedAuthority.class::cast)
                .toList();
        return new User(username, customUser.getPassword(), customUser.isEnabled(), true, true, true, grantedAuthorities);
    }
}
