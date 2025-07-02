package com.cargotracking.shipment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * User Management Service ile iletişim için Feign Client
 * Otomatik carrier assignment için kullanılır
 */
@FeignClient(name = "user-management-service", url = "${services.user-management-service.url:http://localhost:8081}")
public interface UserServiceClient {

    /**
     * Belirli bir kargo şirketinin carrier'larını getirir (Internal endpoint)
     */
    @GetMapping("/api/admin/internal/companies/{companyId}/carriers")
    ApiResponseWrapper<List<UserDto>> getCompanyCarriers(@PathVariable("companyId") Long companyId);

    /**
     * Tüm kargo şirketlerini getirir (Internal endpoint)
     */
    @GetMapping("/api/admin/internal/companies")
    ApiResponseWrapper<List<UserDto>> getShipmentCompanies();

    /**
     * API Response wrapper class
     */
    class ApiResponseWrapper<T> {
        private boolean success;
        private T data;
        private String message;

        // Constructors
        public ApiResponseWrapper() {}

        public ApiResponseWrapper(boolean success, T data, String message) {
            this.success = success;
            this.data = data;
            this.message = message;
        }

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * User DTO for client communication
     */
    class UserDto {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private String address;
        private Long companyId;

        // Constructors
        public UserDto() {}

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Long companyId) {
            this.companyId = companyId;
        }
    }
} 