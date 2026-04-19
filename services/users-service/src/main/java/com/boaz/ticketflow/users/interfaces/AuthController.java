package com.boaz.ticketflow.users.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boaz.ticketflow.common.wrappers.BaseResponse;
import com.boaz.ticketflow.users.application.AuthService;
import com.boaz.ticketflow.users.application.dtos.TokenResponse;
import com.boaz.ticketflow.users.application.dtos.request.RefreshTokenRequest;
import com.boaz.ticketflow.users.docs.AuthApiDocs;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiDocs{

    private final AuthService authService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
        @RequestParam String identifier,
        @RequestParam String code
    ) {
        return ResponseEntity.ok(authService.loginWithOtp(identifier, code));
    }

    @Override
    @PostMapping("/otp/request")
    public ResponseEntity<BaseResponse<String>> requestOtp(@RequestParam String identifier) {
        authService.requestOTP(identifier);
        return ResponseEntity.ok(BaseResponse.success("OTP sent successfully to " + identifier)); // 200 OK, pas de contenu
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request.token());
        return ResponseEntity.ok(response);
    }
}
