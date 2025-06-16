package com.cargotracking.notification_service.dto;

import lombok.Data;

/**
 * User service'den dönen user bilgileri
 */
@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean active;
} 