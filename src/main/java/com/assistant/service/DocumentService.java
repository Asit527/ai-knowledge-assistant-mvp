package com.assistant.service;

import com.assistant.model.Document;
import com.assistant.model.Organization;
import com.assistant.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    private final String UPLOAD_DIR = "uploads/";

    public Document uploadDocument(Organization org, MultipartFile file) throws Exception {
        // 1. Validate max 10 documents per organization
        long docCount = documentRepository.countByOrganizationId(org.getId());
        if (docCount >= 10) {
            throw new RuntimeException("Validation Error: Maximum 10 PDF documents allowed per organization.");
        }

        // 2. Validate file size (Max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("Validation Error: Maximum file size is 10 MB.");
        }

        // 3. Validate file type is PDF
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(".pdf")) {
            throw new RuntimeException("Validation Error: Only PDF files are allowed.");
        }

        // 4. Save file locally
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // 5. Save Document metadata in database
        Document doc = new Document();
        doc.setOrganization(org);
        doc.setFilename(file.getOriginalFilename());
        doc.setFileSize(file.getSize());
        doc.setPageCount(10); // Placeholder: Will be calculated via PDF parser next
        doc.setFilePath(filePath.toString());

        return documentRepository.save(doc);
    }
}
