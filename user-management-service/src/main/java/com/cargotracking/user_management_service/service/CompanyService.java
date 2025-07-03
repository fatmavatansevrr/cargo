package com.cargotracking.user_management_service.service;

import com.cargotracking.user_management_service.model.Company;
import com.cargotracking.user_management_service.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public Company createCompany(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be null or empty.");
        }
        Company company = Company.builder()
                .name(companyName)
                .build();
        return companyRepository.save(company);
    }
} 