// 在 UserService 接口中添加新方法
package com.smile.blue_blog.service;

import com.smile.blue_blog.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    // 原有方法
    List<User> findAll();
    Optional<User> findById(Long id);
    User findByUsername(String username);
    User save(User user);
    void deleteById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User register(User user);
    User login(String username, String password);
    User findByEmail(String email);

    // 新增方法
    Optional<User> findOptionalByUsername(String username);
    boolean updateUserProfile(Long userId, String nickname, String bio, String avatar);
    boolean updateUserStatus(Long userId, Integer status);
    List<User> findUsersByStatus(Integer status);
    boolean isUsernameOrEmailTaken(String username, String email, Long excludeUserId);
    boolean validateCredentials(String username, String password);
}