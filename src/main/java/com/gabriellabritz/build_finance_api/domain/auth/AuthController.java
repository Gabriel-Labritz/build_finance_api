package com.gabriellabritz.build_finance_api.domain.auth;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthLoginRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthRegisterRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthRegisterResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.VerifiedUserResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/verify-account")
    public ResponseEntity<VerifiedUserResponseDto> verifyAccount(@RequestParam String token) {
        return ResponseEntity.ok().body(authService.verifyAccount(token));
    }

    @PostMapping("/sign-in")
    public ResponseEntity login(@RequestBody @Valid AuthLoginRequestDto authLoginRequestDto) {
        authService.login(authLoginRequestDto);
        return ResponseEntity.ok().build();
    }
}
