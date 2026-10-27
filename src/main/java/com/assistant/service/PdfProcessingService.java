package com.assistant.service;

import com.assistant.model.Document;
import com.assistant.model.DocumentChunk;
import com.assistant.repository.DocumentChunkRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class PdfProcessingService {

    @Autowired
    private DocumentChunkRepository chunkRepository;

    public void processPdfToChunks(Document document, File pdfFile) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            int totalPages = doc.getNumberOfPages();
            document.setPageCount(totalPages);

            PDFTextStripper stripper = new PDFTextStripper();

            // Loop through each page, extract text, and create chunks with page numbers
            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(doc);

                if (pageText != null && !pageText.trim().isEmpty()) {
                    DocumentChunk chunk = new DocumentChunk();
                    chunk.setDocument(document);
                    chunk.setOrganizationId(document.getOrganization().getId());
                    chunk.setChunkText(pageText.trim());
                    chunk.setPageNumber(i);
                    chunkRepository.save(chunk);
                }
            }
        }
    }
}
