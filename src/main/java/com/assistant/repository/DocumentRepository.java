package com.assistant.repository;

import com.assistant.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOrganizationId(Long organizationId);
    long countByOrganizationId(Long organizationId);
}
