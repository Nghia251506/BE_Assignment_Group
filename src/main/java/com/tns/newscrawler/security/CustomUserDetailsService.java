package com.tns.newscrawler.security;

import com.tns.newscrawler.entity.Permission;
import com.tns.newscrawler.entity.Role;
import com.tns.newscrawler.entity.User;
import com.tns.newscrawler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Role r = u.getRole();
        if (r != null) {
            // quyền theo role
            authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getCode()));

            // quyền chi tiết theo permission
            if (r.getPermissions() != null) {
                for (Permission p : r.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(p.getCode()));
                }
            }
        }

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                authorities
        );
    }
}