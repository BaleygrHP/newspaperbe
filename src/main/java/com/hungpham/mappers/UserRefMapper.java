package com.hungpham.mappers;

import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.entity.UserEntity;
import com.hungpham.repository.UserRepository;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserRefMapper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UuidBinaryMapper uuidBinaryMapper;

    // Entity -> String (id)
    @Named("userToId")
    public String userToId(UserEntity user) {
        if (user == null) return null;
        return uuidBinaryMapper.toUuid(user.getId());
    }

    // String (id) -> Entity
    @Named("idToUser")
    public UserEntity idToUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) return null;
        // nếu id là UUID bytes thì convert trước rồi findById
        return userRepository.findById(uuidBinaryMapper.toBytes(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }
}
