// src/main/java/com/eventra/repository/FeeTemplateRepository.java
package com.eventra.repository;

import com.eventra.domain.fee.FeeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeeTemplateRepository extends JpaRepository<FeeTemplate, UUID> {

    Optional<FeeTemplate> findByName(String name);
    Optional<FeeTemplate> findByNameAndIsActiveTrue(String name);
    Iterable<FeeTemplate> findAllByIsActiveTrue();
}