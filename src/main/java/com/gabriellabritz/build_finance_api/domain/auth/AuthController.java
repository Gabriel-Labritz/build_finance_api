package com.gabriellabritz.build_finance_api.domain.auth;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthRegisterRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthRegisterResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthRegisterResponseDto> register(@RequestBody @Valid AuthRegisterRequestDto authRegisterRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(authRegisterRequestDto));
    }
}
