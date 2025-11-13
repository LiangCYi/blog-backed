package com.smile.blue_blog.repository;

import com.smile.blue_blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 基础查询方法
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User findByEmail(String email);

    // 新增：使用 Optional 包装的查询方法
    Optional<User> findOptionalByUsername(String username);
    Optional<User> findOptionalByEmail(String email);

    // 新增：状态检查方法
    boolean existsByUsernameAndStatus(String username, Integer status);
    boolean existsByEmailAndStatus(String email, Integer status);

    // 新增：更新最后登录时间
    @Modifying
    @Query("UPDATE User u SET u.lastLoginTime = :lastLoginTime WHERE u.id = :id")
    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    // 新增：更新用户信息
    @Modifying
    @Query("UPDATE User u SET u.nickname = :nickname, u.bio = :bio, u.avatar = :avatar WHERE u.id = :id")
    int updateUserProfile(@Param("id") Long id,
                          @Param("nickname") String nickname,
                          @Param("bio") String bio,
                          @Param("avatar") String avatar);

    // 新增：密码验证方法
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.password = :password AND u.status = 1")
    boolean validateUserCredentials(@Param("username") String username, @Param("password") String password);

    // 新增：根据状态查询用户
    @Query("SELECT u FROM User u WHERE u.status = :status ORDER BY u.createTime DESC")
    List<User> findByStatusOrderByCreateTimeDesc(@Param("status") Integer status);

    // 新增：更新用户状态
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status);

    // 新增：检查用户名或邮箱是否存在（排除指定用户）
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE (u.username = :username OR u.email = :email) AND u.id != :excludeId")
    boolean existsByUsernameOrEmailExcludingId(@Param("username") String username,
                                               @Param("email") String email,
                                               @Param("excludeId") Long excludeId);
}