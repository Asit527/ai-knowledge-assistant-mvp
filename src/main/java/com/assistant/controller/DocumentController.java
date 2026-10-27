package com.assistant.controller;

import com.assistant.model.Document;
import com.assistant.model.Organization;
import com.assistant.repository.OrganizationRepository;
import com.assistant.service.DocumentService;
import com.assistant.service.PdfProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private PdfProcessingService pdfProcessingService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam Long organizationId, @RequestParam("file") MultipartFile file) {
        try {
            Optional<Organization> orgOpt = organizationRepository.findById(organizationId);
            if (orgOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Organization not found!");
            }

            Organization org = orgOpt.get();

            // 1. Validate and save metadata/file using DocumentService
            Document savedDoc = documentService.uploadDocument(org, file);

            // 2. Process PDF text and extract chunks using PdfProcessingService
            File convFile = new File(savedDoc.getFilePath());
            pdfProcessingService.processPdfToChunks(savedDoc, convFile);

            return ResponseEntity.ok("Document uploaded and processed successfully! Document ID: " + savedDoc.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
