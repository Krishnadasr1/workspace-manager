package com.spring.workspacemanager.service;


import com.spring.workspacemanager.dto.RegisterRequest;
import com.spring.workspacemanager.model.Role;
import com.spring.workspacemanager.model.User;
import com.spring.workspacemanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public String register(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already in use");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole()!=null? request.getRole(): Role.USER)
                .build();
        userRepository.save(user);
        return "registered Successfully";

    }

}
