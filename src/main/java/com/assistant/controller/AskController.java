package com.assistant.controller;

import com.assistant.model.Assistant;
import com.assistant.model.DocumentChunk;
import com.assistant.repository.AssistantRepository;
import com.assistant.repository.DocumentChunkRepository;
import com.assistant.service.DecisionEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AskController {

    @Autowired
    private AssistantRepository assistantRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private DecisionEngineService decisionEngineService;

    // Ignore generic filler/question words AND generic context words like "campus", "university", "student", "policy"
    private static final Set<String> STOP_WORDS = Set.of(
        "what", "is", "the", "are", "how", "can", "i", "do", "does", "for",
        "in", "of", "to", "a", "an", "on", "with", "about", "tell", "me", "have",
        "campus", "university", "student", "students", "policy", "requirement", "requirements", "rule", "rules"
    );

    @PostMapping("/ask")
    public ResponseEntity<?> askAssistant(@RequestParam String token, @RequestParam String question) {
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("answer", "Error: Question cannot be empty."));
        }

        // 1. Validate assistant token and identify tenant/organization
        Optional<Assistant> assistantOpt = assistantRepository.findByToken(token);
        if (assistantOpt.isEmpty()) {
            return ResponseEntity.status(403).body("Error: Invalid or unauthorized assistant token.");
        }

        Assistant assistant = assistantOpt.get();
        Long orgId = assistant.getOrganization().getId();

        // 2. Retrieve strictly isolated knowledge chunks for this organization only
        List<DocumentChunk> chunks = documentChunkRepository.findByOrganizationId(orgId);
        if (chunks.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("answer", "The knowledge base does not contain any documents yet.");
            response.put("sourceReference", "None");
            return ResponseEntity.ok(response);
        }

        // 3. Extract specific domain keywords from the question (e.g. "gpa", "swimming", "pool", "tuition", "attendance")
        String[] rawWords = question.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");
        List<String> keywords = new ArrayList<>();
        for (String word : rawWords) {
            if (!word.isEmpty() && !STOP_WORDS.contains(word)) {
                keywords.add(word);
            }
        }

        // 4. Find the best matching chunk AND the exact matching paragraph/section inside that chunk
        DocumentChunk bestChunk = null;
        String bestExcerpt = null;
        int maxMatches = 0;

        for (DocumentChunk chunk : chunks) {
            String fullText = chunk.getChunkText();
            // Split into sections/paragraphs so we return the exact relevant section!
            String[] sections = fullText.split("(?=Page \\d+:)|(?=Section \\d+)|\\n\\s*\\n");

            for (String sec : sections) {
                String secLower = sec.toLowerCase();
                int matches = 0;
                for (String kw : keywords) {
                    if (secLower.contains(kw)) {
                        matches++;
                    }
                }
                if (matches > maxMatches) {
                    maxMatches = matches;
                    bestChunk = chunk;
                    bestExcerpt = sec.trim();
                }
            }
        }

        Map<String, Object> finalResponse = new HashMap<>();

        // 5. Mandatory Fallback (Requirements 9 & 10): If no specific keyword matches, return out-of-scope message
        if (bestChunk == null || maxMatches == 0) {
            Map<String, Object> fallbackDecisions = new HashMap<>();
            fallbackDecisions.put("policyCategory", "UNANSWERABLE_OUT_OF_SCOPE");
            fallbackDecisions.put("isEligible", false);
            fallbackDecisions.put("confidenceScore", 0);

            finalResponse.put("decisions", fallbackDecisions);
            finalResponse.put("answer", "The knowledge base does not contain the required information to answer this question.");
            finalResponse.put("sourceReference", "No relevant document found");
            return ResponseEntity.ok(finalResponse);
        }

        // 6. Execute System 1 decision engine pipeline
        Map<String, Object> decisionResults = decisionEngineService.executeDecisionPipeline(question, 3);
        finalResponse.put("decisions", decisionResults);

        // 7. Build grounded response with document & page citation
        String reference = bestChunk.getDocument().getFilename() + " — Page " + bestChunk.getPageNumber();
        String snippet = bestExcerpt.substring(0, Math.min(250, bestExcerpt.length()));

        finalResponse.put("answer", "Based on our records: " + snippet);
        finalResponse.put("sourceReference", reference);

        return ResponseEntity.ok(finalResponse);
    }
}
