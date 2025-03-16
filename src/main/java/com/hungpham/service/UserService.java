package com.hungpham.service;

import com.hungpham.dtos.UserDto;
import com.hungpham.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUser();
    UserDto getUserById(String id);
    UserEntity updateUser(UserDto userDtos);
    UserEntity newUser (UserDto userDtos);
    UserEntity softDeleteUser(String id);

    UserDto getUserById(UserDto userDto);
}
