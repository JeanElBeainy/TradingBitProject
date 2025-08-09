package com.market.tradingbit.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto {

    @NotEmpty(message = "Name is required")
    private String name;

    @NotEmpty(message = "Email is required")
    @Email
    private String email;

    @Size(min = 6, max = 50, message = "Password must be between 8 and 50 characters")
    private String password;
    private String confirmPassword;
}
