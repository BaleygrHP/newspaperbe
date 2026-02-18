package com.hungpham.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto extends AbstractDto<UserDto>{
    private String email;
    private String displayName;
    private String role;
    private boolean active;

}
