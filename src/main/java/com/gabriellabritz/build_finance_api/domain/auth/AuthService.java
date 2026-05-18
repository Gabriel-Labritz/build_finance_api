package com.gabriellabritz.build_finance_api.domain.auth;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthRegisterRequestDto;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.EmailAlreadyUsedException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(AuthRegisterRequestDto authRegisterRequestDto) {
        if(userRepository.existsByEmailIgnoreCase(authRegisterRequestDto.email())) {
            throw new EmailAlreadyUsedException("O email informado já está em uso, por favor, informe outro email.");
        }
    }
}
