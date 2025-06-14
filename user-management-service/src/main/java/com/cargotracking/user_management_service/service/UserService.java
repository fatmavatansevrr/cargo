package com.cargotracking.user_management_service.service;

import com.cargotracking.user_management_service.dto.ProfileUpdateRequest;
import com.cargotracking.user_management_service.dto.UserRegistrationRequest;
import com.cargotracking.user_management_service.dto.UserResponse;
import com.cargotracking.user_management_service.model.Role;
import com.cargotracking.user_management_service.model.User;
import com.cargotracking.user_management_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User Service - Requirements FR-UM-001 to FR-UM-006
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Kullanıcı kaydı - FR-UM-001
     */
    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        // Kullanıcı adı ve email kontrolü
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email adresi zaten kullanılıyor");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .roles(request.getRoles())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    /**
     * Kullanıcı bulma - FR-UM-002
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    /**
     * Profil görüntüleme - FR-UM-005
     */
    public UserResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return mapToUserResponse(user);
    }

    /**
     * Profil güncelleme - FR-UM-005
     */
    @Transactional
    public UserResponse updateProfile(String username, ProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Email değişikliği kontrolü
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email adresi zaten kullanılıyor");
        }

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    /**
     * Şifre sıfırlama token oluşturma - FR-UM-006
     */
    @Transactional
    public String initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Bu email adresi ile kayıtlı kullanıcı bulunamadı"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(24)); // 24 saat geçerli

        userRepository.save(user);
        return resetToken;
    }

    /**
     * Şifre sıfırlama - FR-UM-006
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Geçersiz şifre sıfırlama token'ı"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Şifre sıfırlama token'ı geçersiz");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }

    /**
     * Şifre değiştirme
     */
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mevcut şifre yanlış");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Tüm kullanıcıları listele (Admin özelliği) - FR-UM-004.4
     */
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID'ye göre kullanıcı getir (Admin özelliği) - FR-UM-004.4
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return mapToUserResponse(user);
    }

    /**
     * Kullanıcı arama (Admin özelliği) - FR-UM-004.4
     */
    public List<UserResponse> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }
        
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(user -> 
                    user.getUsername().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchTerm.toLowerCase())) ||
                    (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchTerm.toLowerCase()))
                )
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Kullanıcı durumu değiştirme (Admin özelliği)
     */
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    /**
     * Entity'den DTO'ya mapping
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(user.getRoles())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
