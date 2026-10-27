package com.assistant.repository;

import com.assistant.model.Assistant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssistantRepository extends JpaRepository<Assistant, Long> {
    Optional<Assistant> findByToken(String token);
    Optional<Assistant> findByOrganizationId(Long organizationId);
}
