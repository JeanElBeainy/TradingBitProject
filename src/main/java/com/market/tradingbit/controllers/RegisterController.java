package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.RegisterDto;
import com.market.tradingbit.entities.Balance;
import com.market.tradingbit.entities.Role;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.mappers.UserMapper;
import com.market.tradingbit.repositories.BalanceRepository;
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
    private final BalanceRepository balanceRepository;
    private final UserMapper userMapper;

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
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user = userMapper.toEntity(registerDto);
            user.setPassword(encoder.encode(user.getPassword()));
            user.setRole(Role.USER);
            user.setCreatedAt(new Date());

            User savedUser = userRepository.save(user);

            Balance balance = Balance
                    .builder()
                    .id(savedUser.getId())
                    .cryptoBalance(0.0)
                    .stockBalance(0.0)
                    .usdBalance(100_000)
                    .build();
            balance.setTotalBalance(balance.getCryptoBalance() + balance.getStockBalance() + balance.getUsdBalance());
            balanceRepository.save(balance);

        } catch (Exception e) {
            System.out.println("Exception with POST register: " + e.getMessage());
            return "signup";
        }
        return "signup";
    }
}
