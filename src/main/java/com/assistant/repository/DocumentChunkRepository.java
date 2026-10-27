package com.assistant.repository;

import com.assistant.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    // Crucial for multi-tenant isolation: fetch chunks strictly by organization ID
    List<DocumentChunk> findByOrganizationId(Long organizationId);
}
