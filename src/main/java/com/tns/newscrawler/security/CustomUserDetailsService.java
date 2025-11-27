package com.tns.newscrawler.security;

import com.tns.newscrawler.entity.Role;
import com.tns.newscrawler.entity.User;
import com.tns.newscrawler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Giờ thì role + permissions đã được load sẵn → không Lazy nữa!
        var authorities = user.getRole().getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.getCode()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }

    // QUAN TRỌNG: Phải thêm prefix "ROLE_" trước roleCode
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        String roleCode = user.getRole().getCode(); // Ví dụ: "ADMIN", "USER"

        // Thêm prefix "ROLE_" để Spring Security hasRole() hoạt động
        String authorityName = "ROLE_" + roleCode;

        return Collections.singletonList(new SimpleGrantedAuthority(authorityName));
    }
}