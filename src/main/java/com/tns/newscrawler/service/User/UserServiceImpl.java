package com.tns.newscrawler.service.User;

import com.tns.newscrawler.dto.User.UserCreateRequest;
import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.User.UserUpdateRequest;
import com.tns.newscrawler.entity.Role;
import com.tns.newscrawler.entity.User;
import com.tns.newscrawler.mapper.User.UserMapper;
import com.tns.newscrawler.repository.RoleRepository;
import com.tns.newscrawler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    // ============== CÁC HÀM TRONG INTERFACE ==============

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepo.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public com.tns.newscrawler.entity.User findByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllActiveUsers() {
        // Lấy tất cả người dùng đang hoạt động (hoặc bất kỳ điều kiện nào bạn cần)
        return userRepo.findAll().stream()
                .filter(User::getIsActive)
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return UserMapper.toDto(u);
    }

    @Override
    @Transactional
    public UserDto create(UserCreateRequest req) {
        User u = new User();
        u.setUsername(req.getUsername());

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        if (req.getRole() != null) {
            Role role = roleRepo.findByCode(req.getRole())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + req.getRole()));
            u.setRole(role); // ManyToOne → set trực tiếp
        }

        User saved = userRepo.save(u);
        return UserMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserUpdateRequest req) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            u.setUsername(req.getFullName());
        }

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        if (req.getRole() != null) {
            Role role = roleRepo.findByCode(req.getRole())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + req.getRole()));
            u.setRole(role);
        }

        User saved = userRepo.save(u);
        return UserMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepo.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepo.deleteById(id);
    }

    // ============== CÁC HÀM PHỤ TRỢ CHO SECURITY ==============

    @Transactional(readOnly = true)
    public UserDto getByUsername(String username) {
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return UserMapper.toDto(u);
    }

    @Transactional(readOnly = true)
    public User getDomainUserByUsername(String username) {
        // hàm này nếu CustomUserDetailsService muốn dùng directly entity
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName();
        return getByUsername(username);
    }

    @Transactional
    public UserDto assignRoleToUser(Long userId, String roleCode) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Role role = roleRepo.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleCode));

        user.setRole(role);             // ✅ 1 user 1 role
        User saved = userRepo.save(user);

        return UserMapper.toDto(saved);
    }
}
