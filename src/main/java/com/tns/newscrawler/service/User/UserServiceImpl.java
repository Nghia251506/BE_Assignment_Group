package com.tns.newscrawler.service.User;

import com.tns.newscrawler.dto.User.UserCreateRequest;
import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.User.UserUpdateRequest;
import com.tns.newscrawler.entity.Tenant;
import com.tns.newscrawler.entity.User;
import com.tns.newscrawler.mapper.User.UserMapper;
import com.tns.newscrawler.repository.TenantRepository;
import com.tns.newscrawler.repository.UserRepository;
import com.tns.newscrawler.service.User.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserRepository userRepository,
                           TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public List<UserDto> getByTenant(Long tenantId) {
        return userRepository.findByTenant_Id(tenantId)
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto create(UserCreateRequest req) {
        // 1. check tenant
        Tenant tenant = tenantRepository.findById(req.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // 2. check limit
        long currentActiveUsers = userRepository.countByTenant_IdAndIsActiveTrue(tenant.getId());
        if (currentActiveUsers >= tenant.getMaxUsers()) {
            throw new RuntimeException("Tenant reached max users: " + tenant.getMaxUsers());
        }

        // 3. check username
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 4. create entity
        User user = User.builder()
                .tenant(tenant)
                .username(req.getUsername())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .email(req.getEmail())
                .role(req.getRole() != null
                        ? User.UserRole.valueOf(req.getRole())
                        : User.UserRole.EDITOR)
                .isActive(true)
                .build();

        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto update(Long id, UserUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getRole() != null) user.setRole(User.UserRole.valueOf(req.getRole()));
        if (req.getIsActive() != null) user.setIsActive(req.getIsActive());

        return UserMapper.toDto(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
