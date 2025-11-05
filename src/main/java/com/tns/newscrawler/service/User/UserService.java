package com.tns.newscrawler.service.User;

import com.tns.newscrawler.dto.User.UserCreateRequest;
import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.User.UserUpdateRequest;

import java.util.List;

public interface UserService {
    List<UserDto> getAll();
    List<UserDto> getByTenant(Long tenantId);
    UserDto getById(Long id);
    UserDto create(UserCreateRequest req);
    UserDto update(Long id, UserUpdateRequest req);
    void delete(Long id);
}
