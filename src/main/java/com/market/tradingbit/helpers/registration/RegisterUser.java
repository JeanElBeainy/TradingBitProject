package com.market.tradingbit.helpers.registration;

import com.market.tradingbit.dtos.RegisterDto;
import com.market.tradingbit.entities.*;
import com.market.tradingbit.mappers.UserMapper;
import com.market.tradingbit.repositories.AssetRepository;
import com.market.tradingbit.repositories.BalanceRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.math.BigDecimal;
import java.util.Date;

@Component
@AllArgsConstructor
public class RegisterUser {

    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;
    private final UserMapper userMapper;
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final BigDecimal StartingBalance = BigDecimal.valueOf(1_000_000);

    public String registerUser(Model model, RegisterDto registerDto, BindingResult bindingResult) {
        if(!registerDto.getPassword().equals(registerDto.getConfirmPassword()))
            bindingResult.addError(new FieldError("registerDto", "confirmPassword", "Passwords do not match"));

        if(userRepository.findByEmail(registerDto.getEmail()) != null)
            bindingResult.addError(new FieldError("registerDto", "email", "Email already exists"));

        if(bindingResult.hasErrors()) {
            model.addAttribute("registerDto", nullRegisterDto(registerDto));
            return "signup";
        }
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
                    .cryptoBalance(BigDecimal.valueOf(0))
                    .stockBalance(BigDecimal.valueOf(0))
                    .usdBalance(StartingBalance)
                    .totalBalance(StartingBalance)
                    .totalVolume(BigDecimal.valueOf(0))
                    .build();
            balanceRepository.save(balance);

            assetRepository.save(Asset.builder()
                    .symbol("US Dollar")
                    .name("US Dollar Balance")
                    .build());

            portfolioRepository.save(Portfolio.builder()
                    .purchaseType(Type.USD)
                    .symbol("US Dollar")
                    .quantity(StartingBalance)
                    .userId(savedUser.getId())
                    .build());


        } catch (Exception e) {
            System.out.println("Exception with POST register: " + e.getMessage());
            return "signup";
        }
        model.addAttribute("success", true);
        return "signup";
    }

    private RegisterDto nullRegisterDto(RegisterDto registerDto) {
        registerDto.setName(null);
        registerDto.setPassword(null);
        registerDto.setConfirmPassword(null);
        return registerDto;
    }
}
