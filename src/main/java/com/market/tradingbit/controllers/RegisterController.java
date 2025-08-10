package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.RegisterDto;
import com.market.tradingbit.entities.Role;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Date;

@Controller
@AllArgsConstructor
@RequestMapping("/signup")
public class RegisterController {

    private final UserRepository userRepository;

    @GetMapping
    public String register(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "signup";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute RegisterDto registerDto, BindingResult bindingResult) {
        if(!registerDto.getPassword().equals(registerDto.getConfirmPassword()))
            bindingResult.addError(new FieldError("registerDto", "confirmPassword", "Passwords do not match"));

        if(userRepository.findByEmail(registerDto.getEmail()) != null)
            bindingResult.addError(new FieldError("registerDto", "email", "Email already exists"));

        if(bindingResult.hasErrors())
            return "signup";
        try {
            User user = User.builder()
                    .name(registerDto.getName())
                    .email(registerDto.getEmail())
                    .password(new BCryptPasswordEncoder().encode(registerDto.getPassword()))
                    .role(Role.USER)
                    .createdAt(new Date())
                    .build();
            System.out.println(user.toString());
            userRepository.save(user);
        } catch (Exception e) {
            System.out.println("Exception with POST register: " + e.getMessage());
            return "signup";
        }
        return "signup";
    }
}
