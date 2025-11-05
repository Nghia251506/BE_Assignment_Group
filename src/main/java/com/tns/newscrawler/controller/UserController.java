package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.User.UserCreateRequest;
import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.User.UserUpdateRequest;
import com.tns.newscrawler.service.User.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    // lấy user theo tenant
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<UserDto>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(userService.getByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserCreateRequest req) {
        return ResponseEntity.ok(userService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id,
                                          @RequestBody UserUpdateRequest req) {
        return ResponseEntity.ok(userService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
