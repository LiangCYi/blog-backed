package com.smile.blue_blog.service.impl;

import com.smile.blue_blog.entity.User;
import com.smile.blue_blog.repository.UserRepository;
import com.smile.blue_blog.service.UserService;
import com.smile.blue_blog.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

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
        // 设置创建时间或更新时间
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
        // 注册时的业务逻辑
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        return save(user);
    }

    @Override
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        //检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 验证密码（这里先简单比较，实际应该使用加密比较）
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}