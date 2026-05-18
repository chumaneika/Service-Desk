package com.bachelor.service_desk.config;



import com.bachelor.service_desk.dto.security.AuthResponseDTO;
import com.bachelor.service_desk.dto.security.LoginRequestDTO;
import com.bachelor.service_desk.dto.security.RefreshTokenRequestDTO;
import com.bachelor.service_desk.entity.UserEntity;
import com.bachelor.service_desk.security.JwtService;
import com.bachelor.service_desk.security.UserDetailsImpl;
import com.bachelor.service_desk.security.UserDetailsServiceImpl;
import com.bachelor.service_desk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.numberPhone(),
                        request.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String token = jwtService.generateAccessToken(userDetails, userDetails.getId());
        String refreshToken = jwtService.generateRefreshToken(userDetails, userDetails.getId());
        userService.rotateRefreshToken(
                userDetails.getId(),
                refreshToken,
                jwtService.extractExpirationInstant(refreshToken)
        );

        return ResponseEntity.ok(new AuthResponseDTO(
                token,
                refreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                null,
                null
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO request) {
        String refreshToken = request.refreshToken();
        String numberPhone = jwtService.extractLogin(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(numberPhone);
        UserDetailsImpl details = (UserDetailsImpl) userDetails;

        if (!jwtService.validateRefreshToken(refreshToken, details)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        if (!userService.isRefreshTokenValid(details.getId(), refreshToken)) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        String newAccessToken = jwtService.generateAccessToken(details, details.getId());
        String newRefreshToken = jwtService.generateRefreshToken(details, details.getId());
        userService.rotateRefreshToken(
                details.getId(),
                newRefreshToken,
                jwtService.extractExpirationInstant(newRefreshToken)
        );

        return ResponseEntity.ok(new AuthResponseDTO(
                newAccessToken,
                newRefreshToken,
                details.getId(),
                details.getUsername(),
                null,
                null
        ));
    }
}
