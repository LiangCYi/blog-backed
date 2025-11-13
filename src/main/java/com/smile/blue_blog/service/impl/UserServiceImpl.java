package com.smile.blue_blog.service.impl;

import com.smile.blue_blog.entity.User;
import com.smile.blue_blog.repository.UserRepository;
import com.smile.blue_blog.service.UserService;
import com.smile.blue_blog.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User register(User user) {
        // 使用 Repository 方法进行验证
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 设置默认值
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        return userRepository.save(user);
    }

    @Override
    public User login(String username, String password) {
        // 使用 Repository 的密码验证方法
        boolean isValid = userRepository.validateUserCredentials(username, password);
        if (!isValid) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 获取用户信息
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 更新最后登录时间
        userRepository.updateLastLoginTime(user.getId(), LocalDateTime.now());

        return user;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ========== 新增方法 ==========

    /**
     * 使用 Optional 的安全查询
     */
    public Optional<User> findOptionalByUsername(String username) {
        return userRepository.findOptionalByUsername(username);
    }

    /**
     * 更新用户资料
     */
    public boolean updateUserProfile(Long userId, String nickname, String bio, String avatar) {
        int updated = userRepository.updateUserProfile(userId, nickname, bio, avatar);
        return updated > 0;
    }

    /**
     * 更新用户状态
     */
    public boolean updateUserStatus(Long userId, Integer status) {
        int updated = userRepository.updateUserStatus(userId, status);
        return updated > 0;
    }

    /**
     * 根据状态查询用户
     */
    public List<User> findUsersByStatus(Integer status) {
        return userRepository.findByStatusOrderByCreateTimeDesc(status);
    }

    /**
     * 检查用户名或邮箱是否被其他用户使用
     */
    public boolean isUsernameOrEmailTaken(String username, String email, Long excludeUserId) {
        return userRepository.existsByUsernameOrEmailExcludingId(username, email, excludeUserId);
    }

    /**
     * 验证用户凭据（不抛出异常版本）
     */
    public boolean validateCredentials(String username, String password) {
        return userRepository.validateUserCredentials(username, password);
    }
}