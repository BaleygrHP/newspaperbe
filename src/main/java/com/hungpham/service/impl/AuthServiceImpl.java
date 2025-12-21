package com.hungpham.service.impl;

import com.hungpham.common.exception.BadRequestException;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.dtos.UserDto;
import com.hungpham.entity.UserEntity;
import com.hungpham.mappers.UserMapper;
import com.hungpham.mappers.UuidBinaryMapper;
import com.hungpham.repository.UserRepository;

import com.hungpham.requests.auth.LoginRequest;
import com.hungpham.response.auth.LoginResponse;
import com.hungpham.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UuidBinaryMapper uuidBinaryMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        if (req == null) throw new BadRequestException("Body is required");
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new BadRequestException("email is required");
        }
        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            throw new BadRequestException("password is required");
        }

        String email = req.getEmail().trim().toLowerCase();
        log.info("[Auth][Login] email={}", email);

        UserEntity user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // giả định UserEntity có field passwordHash
        String hash = user.getPasswordHash();
        if (hash == null || !encoder.matches(req.getPassword(), hash)) {
            log.warn("[Auth][Login] invalid credentials email={}", email);
            throw new BadRequestException("Invalid email or password");
        }

        UserDto dto = userMapper.toDto(user);

        LoginResponse res = new LoginResponse();
        res.setUser(dto);
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto me(String actorUserId) {

        if (actorUserId == null || actorUserId.trim().isEmpty()) {
            throw new BadRequestException("Missing X-Actor-UserId header");
        }

        byte[] userId = uuidBinaryMapper.toBytes(actorUserId.trim());

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found: " + actorUserId
                ));

        if (!user.isActive()) {
            throw new BadRequestException("User is inactive");
        }

        return userMapper.toDto(user);
    }
}
