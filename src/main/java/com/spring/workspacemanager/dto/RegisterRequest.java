package com.spring.workspacemanager.dto;


import com.spring.workspacemanager.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid Email format")
    @NotNull(message = "Email is required")
    private String email;

    @Size(min = 6,message = "password must be atleast 6 characters")
    @NotNull(message = "password cannot be empty")
    private String password;

    private Role role;

}
