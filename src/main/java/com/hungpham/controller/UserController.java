package com.hungpham.controller;

import com.hungpham.dtos.UserDto;
import com.hungpham.entity.UserEntity;
import com.hungpham.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("auth/admin")
// remove gấp sau khi config auth2.0
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend origin
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/new-user")
    public UserEntity newUser(@RequestBody UserDto userDtos) {
        return userService.newUser(userDtos);
    }

    @PutMapping("/update-user")
    public UserEntity updatetUser(@RequestBody UserDto userDtos) {
        return userService.updateUser(userDtos);
    }

    @DeleteMapping
    public UserEntity softDeletedUser(@RequestBody String id) {
        return userService.softDeleteUser(id);
    }

    @GetMapping("/get-all")
    public List<UserDto> getAllUser(){
        return userService.getAllUser();
    }

    @GetMapping("/get")
    public UserDto getUserById(@RequestParam String id){
        return userService.getUserById(id);
    }

    @PostMapping("/login")
    public UserDto login(@RequestBody UserDto userDtos){
        return userService.getUserById(userDtos);
    }


}
