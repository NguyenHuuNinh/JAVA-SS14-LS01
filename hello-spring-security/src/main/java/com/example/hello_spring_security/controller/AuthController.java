package com.example.hello_spring_security.controller;

import com.example.hello_spring_security.dto.JwtResponse;
import com.example.hello_spring_security.dto.LoginRequest;
import com.example.hello_spring_security.model.User;
import com.example.hello_spring_security.repository.UserRepository;
import com.example.hello_spring_security.security.JwtProvider;
import com.example.hello_spring_security.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider jwtProvider;

    // Tự động tạo 1 user mẫu trong DB H2 khi chạy ứng dụng để test
    @Bean
    public CommandLineRunner initUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                userRepository.save(admin);
            }
        };
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        // 1. Xác thực username & password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2. Lấy thông tin UserPrincipal sau khi xác thực thành công
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 3. Tạo chuỗi JWT
        String token = jwtProvider.generateToken(userPrincipal);

        // 4. Trả về JSON chứa token
        return ResponseEntity.ok(new JwtResponse(token, userPrincipal.getUsername()));
    }
}