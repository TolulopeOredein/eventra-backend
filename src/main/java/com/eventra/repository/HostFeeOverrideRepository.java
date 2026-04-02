// src/main/java/com/eventra/repository/HostFeeOverrideRepository.java
package com.eventra.repository;

import com.eventra.domain.fee.HostFeeOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostFeeOverrideRepository extends JpaRepository<HostFeeOverride, UUID> {

    Optional<HostFeeOverride> findByHostId(UUID hostId);
}