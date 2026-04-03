package com.market.tradingbit.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto {

    @NotEmpty(message = "Name is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    private String name;

    @NotEmpty(message = "Nice try, but the email field is required")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Email(message = "Invalid email address")
    private String email;

    @NotEmpty(message = "Nice try, but the password is required")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;

    private String confirmPassword;
}
