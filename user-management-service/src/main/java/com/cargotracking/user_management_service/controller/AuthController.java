package com.cargotracking.user_management_service.controller;

import com.cargotracking.user_management_service.dto.*;
import com.cargotracking.user_management_service.model.User;
import com.cargotracking.user_management_service.security.JwtUtil;
import com.cargotracking.user_management_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication Controller - Requirements FR-UM-001 to FR-UM-006
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Kullanıcı kimlik doğrulama API'leri")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Root endpoint - Health check ve test için
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "service", "User Management Service",
            "status", "Running",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    /**
     * Kullanıcı kaydı - FR-UM-001
     */
    @PostMapping("/register")
    @Operation(summary = "Kullanıcı Kaydı", description = "Yeni kullanıcı hesabı oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcı başarıyla kaydedildi", 
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Geçersiz veriler", 
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            UserResponse userResponse = userService.register(request);
            return ResponseEntity.ok(Map.of(
                "message", "Kullanıcı başarıyla kaydedildi",
                "user", userResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Kullanıcı girişi - FR-UM-002
     */
    @PostMapping("/login")
    @Operation(summary = "Kullanıcı Girişi", description = "Kullanıcı adı/email ve şifre ile giriş yapar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Giriş başarılı", 
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
            
            User user = userService.findByUsernameOrEmail(request.getUsernameOrEmail())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
            
            String token = jwtUtil.generateToken(user.getUsername(), user.getRoles());
            UserResponse userResponse = userService.getUserProfile(user.getUsername());
            
            return ResponseEntity.ok(AuthResponse.of(token, userResponse));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Geçersiz kullanıcı adı veya şifre"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Kullanıcı çıkışı - FR-UM-003
     */
    @PostMapping("/logout")
    @Operation(summary = "Kullanıcı Çıkışı", description = "Güvenli çıkış yapar")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> logout() {
        // Stateless JWT kullandığımız için client-side'da token'ı silmek yeterli
        return ResponseEntity.ok(Map.of("message", "Başarıyla çıkış yapıldı"));
    }

    /**
     * Profil görüntüleme - FR-UM-005
     */
    @GetMapping("/profile")
    @Operation(summary = "Profil Görüntüleme", description = "Mevcut kullanıcının profil bilgilerini getirir")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profil bilgileri başarıyla getirildi", 
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Yetkisiz erişim")
    })
    public ResponseEntity<?> getProfile() {
        try {
            String username = getCurrentUsername();
            UserResponse userResponse = userService.getUserProfile(username);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Profil güncelleme - FR-UM-005
     */
    @PutMapping("/profile")
    @Operation(summary = "Profil Güncelleme", description = "Kullanıcı profil bilgilerini günceller")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profil başarıyla güncellendi"),
        @ApiResponse(responseCode = "400", description = "Geçersiz veriler"),
        @ApiResponse(responseCode = "401", description = "Yetkisiz erişim")
    })
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        try {
            String username = getCurrentUsername();
            UserResponse userResponse = userService.updateProfile(username, request);
            return ResponseEntity.ok(Map.of(
                "message", "Profil başarıyla güncellendi",
                "user", userResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Şifre sıfırlama isteği - FR-UM-006
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Şifre Sıfırlama İsteği", description = "Email ile şifre sıfırlama isteği gönderir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Şifre sıfırlama bağlantısı gönderildi"),
        @ApiResponse(responseCode = "400", description = "Geçersiz email adresi")
    })
    public ResponseEntity<?> initiatePasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        try {
            String resetToken = userService.initiatePasswordReset(request.getEmail());
            // Gerçek uygulamada burada email gönderilir
            return ResponseEntity.ok(Map.of(
                "message", "Şifre sıfırlama bağlantısı email adresinize gönderildi",
                "resetToken", resetToken // Test için - production'da gönderilmez
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Şifre sıfırlama - FR-UM-006
     */
    @PostMapping("/reset-password/{token}")
    @Operation(summary = "Şifre Sıfırlama", description = "Token ile yeni şifre belirler")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Şifre başarıyla sıfırlandı"),
        @ApiResponse(responseCode = "400", description = "Geçersiz token veya şifre")
    })
    public ResponseEntity<?> resetPassword(@PathVariable String token, 
                                         @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Şifre en az 6 karakter olmalıdır"));
            }
            
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Şifre başarıyla sıfırlandı"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Şifre değiştirme
     */
    @PostMapping("/change-password")
    @Operation(summary = "Şifre Değiştirme", description = "Mevcut şifre ile yeni şifre belirler")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Şifre başarıyla değiştirildi"),
        @ApiResponse(responseCode = "400", description = "Geçersiz mevcut şifre"),
        @ApiResponse(responseCode = "401", description = "Yetkisiz erişim")
    })
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String username = getCurrentUsername();
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Yeni şifre en az 6 karakter olmalıdır"));
            }
            
            userService.changePassword(username, oldPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Şifre başarıyla değiştirildi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mevcut kullanıcının username'ini al
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
