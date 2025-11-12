package com.smile.blue_blog.dto;

import com.smile.blue_blog.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String bio;
    private String role;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;

    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setBio(user.getBio());
        dto.setRole(user.getRole());
        dto.setCreateTime(user.getCreateTime());
        dto.setLastLoginTime(user.getLastLoginTime());
        return dto;
    }


    public static UserDTO fromEntityWithDisplayName(User user) {
        UserDTO dto = fromEntity(user);
        if (dto != null && dto.getNickname() == null) {
            dto.setNickname(user.getUsername());
        }
        return dto;
    }
}