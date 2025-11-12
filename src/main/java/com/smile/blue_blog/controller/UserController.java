package com.smile.blue_blog.controller;

import com.smile.blue_blog.dto.UserDTO;
import com.smile.blue_blog.entity.User;
import com.smile.blue_blog.service.UserService;
import com.smile.blue_blog.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    // 常量定义
    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 20;
    private static final int BIO_MAX_LENGTH = 200;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final String UPLOAD_DIR = "/uploads/avatars/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAllUsers() {
        try {
            List<User> users = userService.findAll();
            List<UserDTO> userDTOs = users.stream()
                    .map(UserDTO::fromEntity)
                    .toList();
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        return user.map(u -> ResponseEntity.ok(UserDTO.fromEntity(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 获取用户信息（根据用户名）
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(UserDTO.fromEntity(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 获取当前用户信息
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Optional<User> userOptional = userService.findById(userId);

            if (userOptional.isPresent()) {
                UserDTO userDTO = UserDTO.fromEntity(userOptional.get());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", userDTO);
                return ResponseEntity.ok(response);
            } else {
                return buildErrorResponse("用户不存在", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return buildErrorResponse("获取用户信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            // 手动验证参数
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return buildErrorResponse("用户名不能为空", HttpStatus.BAD_REQUEST);
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return buildErrorResponse("密码不能为空", HttpStatus.BAD_REQUEST);
            }
            if (user.getPassword().length() < PASSWORD_MIN_LENGTH) {
                return buildErrorResponse("密码长度不能少于" + PASSWORD_MIN_LENGTH + "位", HttpStatus.BAD_REQUEST);
            }

            User registeredUser = userService.register(user);
            UserDTO userDTO = UserDTO.fromEntity(registeredUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                return buildErrorResponse("用户名不能为空", HttpStatus.BAD_REQUEST);
            }
            if (password == null || password.trim().isEmpty()) {
                return buildErrorResponse("密码不能为空", HttpStatus.BAD_REQUEST);
            }

            User user = userService.login(username, password);
            String token = jwtUtils.generateToken(user.getUsername(), user.getId());

            // 使用DTO隐藏敏感信息
            UserDTO userDTO = UserDTO.fromEntity(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("data", Map.of(
                    "user", userDTO,
                    "token", token
            ));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 上传头像
    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          HttpServletRequest request) {
        try {
            System.out.println("=== 开始上传头像 ===");
            System.out.println("文件名: " + file.getOriginalFilename());
            System.out.println("文件大小: " + file.getSize());
            System.out.println("文件类型: " + file.getContentType());

            // 验证文件是否为空
            if (file.isEmpty()) {
                return buildErrorResponse("文件不能为空", HttpStatus.BAD_REQUEST);
            }

            // 验证文件类型
            if (!file.getContentType().startsWith("image/")) {
                return buildErrorResponse("只能上传图片文件", HttpStatus.BAD_REQUEST);
            }

            // 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                return buildErrorResponse("文件大小不能超过5MB", HttpStatus.BAD_REQUEST);
            }

            // 获取项目根目录的绝对路径
            String projectRoot = System.getProperty("user.dir");
            System.out.println("项目根目录: " + projectRoot);

            // 生成唯一文件名
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + fileExtension;

            // 保存路径 - 使用绝对路径
            String uploadDir = projectRoot + UPLOAD_DIR;
            File uploadPath = new File(uploadDir);

            // 确保目录存在
            if (!uploadPath.exists() && !uploadPath.mkdirs()) {
                return buildErrorResponse("无法创建上传目录", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 保存文件
            String filePath = uploadDir + fileName;
            File dest = new File(filePath);
            file.transferTo(dest);
            System.out.println("文件保存成功");

            // 从请求属性获取用户名
            String username = (String) request.getAttribute("username");
            System.out.println("上传用户: " + username);

            if (username == null) {
                return buildErrorResponse("用户未认证", HttpStatus.UNAUTHORIZED);
            }

            // 更新用户头像URL
            User user = userService.findByUsername(username);
            if (user == null) {
                return buildErrorResponse("用户不存在", HttpStatus.NOT_FOUND);
            }

            // 存储相对路径，便于前端访问
            String relativePath = UPLOAD_DIR + fileName;
            user.setAvatar(relativePath);
            userService.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "头像上传成功");
            response.put("avatarUrl", relativePath);

            System.out.println("头像上传成功: " + relativePath);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("头像上传失败: " + e.getMessage());
            e.printStackTrace();
            return buildErrorResponse("头像上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 修改昵称
    @PutMapping("/update-nickname")
    public ResponseEntity<?> updateNickname(@RequestBody Map<String, String> nicknameRequest,
                                            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");

            System.out.println("=== 开始修改昵称 ===");
            System.out.println("用户ID: " + userId);
            System.out.println("用户名: " + username);

            if (userId == null) {
                return buildErrorResponse("用户未认证", HttpStatus.UNAUTHORIZED);
            }

            String newNickname = nicknameRequest.get("nickname");

            // 验证昵称参数
            if (newNickname == null || newNickname.trim().isEmpty()) {
                return buildErrorResponse("昵称不能为空", HttpStatus.BAD_REQUEST);
            }

            newNickname = newNickname.trim();

            // 验证昵称长度
            if (newNickname.length() < NICKNAME_MIN_LENGTH || newNickname.length() > NICKNAME_MAX_LENGTH) {
                return buildErrorResponse("昵称长度应在" + NICKNAME_MIN_LENGTH + "-" + NICKNAME_MAX_LENGTH + "个字符之间",
                        HttpStatus.BAD_REQUEST);
            }

            // 查找用户
            Optional<User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                return buildErrorResponse("用户不存在", HttpStatus.NOT_FOUND);
            }

            User user = userOptional.get();

            // 检查昵称是否与当前相同
            if (newNickname.equals(user.getNickname())) {
                return buildErrorResponse("新昵称与当前昵称相同", HttpStatus.BAD_REQUEST);
            }

            // 更新昵称
            String oldNickname = user.getNickname();
            user.setNickname(newNickname);
            User updatedUser = userService.save(user);

            System.out.println("昵称修改成功：" + oldNickname + " -> " + newNickname);

            // 使用DTO返回数据
            UserDTO userDTO = UserDTO.fromEntity(updatedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "昵称修改成功");
            response.put("data", userDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("昵称修改失败: " + e.getMessage());
            e.printStackTrace();
            return buildErrorResponse("昵称修改失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 修改个人简介
    @PutMapping("/update-bio")
    public ResponseEntity<?> updateBio(@RequestBody Map<String, String> bioRequest,
                                       HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null) {
                return buildErrorResponse("用户未认证", HttpStatus.UNAUTHORIZED);
            }

            String newBio = bioRequest.get("bio");

            // 验证个人简介长度
            if (newBio != null && newBio.length() > BIO_MAX_LENGTH) {
                return buildErrorResponse("个人简介长度不能超过" + BIO_MAX_LENGTH + "个字符",
                        HttpStatus.BAD_REQUEST);
            }

            // 查找用户
            Optional<User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                return buildErrorResponse("用户不存在", HttpStatus.NOT_FOUND);
            }

            User user = userOptional.get();
            user.setBio(newBio != null ? newBio.trim() : "");
            User updatedUser = userService.save(user);

            // 使用DTO返回数据
            UserDTO userDTO = UserDTO.fromEntity(updatedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "个人简介修改成功");
            response.put("data", userDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("个人简介修改失败: " + e.getMessage());
            e.printStackTrace();
            return buildErrorResponse("个人简介修改失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 检查用户名是否存在
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    // 检测邮箱是否存在
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    // 辅助方法

    /**
     * 构建错误响应
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}