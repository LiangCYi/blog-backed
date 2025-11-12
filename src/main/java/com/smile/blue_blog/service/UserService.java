package com.smile.blue_blog.service;

import com.smile.blue_blog.entity.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
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
}