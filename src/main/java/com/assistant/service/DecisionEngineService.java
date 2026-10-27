package com.assistant.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class DecisionEngineService {

    // System 1 Decision Model: Boolean Eligibility Check
    public boolean evaluateBooleanEligibility(String condition, int userValue, int threshold) {
        if ("GREATER_THAN".equalsIgnoreCase(condition)) {
            return userValue > threshold;
        } else if ("AT_LEAST".equalsIgnoreCase(condition)) {
            return userValue >= threshold;
        }
        return userValue == threshold;
    }

    // System 1 Decision Model: Choice / Policy Categorization
    public String evaluateChoicePolicy(String userQuery) {
        String queryLower = userQuery.toLowerCase();
        if (queryLower.contains("scholarship") || queryLower.contains("merit")) {
            return "POLICY_SCHOLARSHIP";
        } else if (queryLower.contains("fee") || queryLower.contains("tuition")) {
            return "POLICY_TUITION_FEES";
        } else if (queryLower.contains("attendance") || queryLower.contains("leave")) {
            return "POLICY_ATTENDANCE";
        }
        return "POLICY_GENERAL";
    }

    // System 1 Decision Model: Score Calculation
    public int calculateSupportScore(String chunkText, String keywords) {
        int score = 0;
        if (chunkText == null || keywords == null) return score;
        
        String[] keys = keywords.toLowerCase().split(",");
        for (String key : keys) {
            if (chunkText.toLowerCase().contains(key.trim())) {
                score += 25;
            }
        }
        return Math.min(score, 100); // Cap score at 100
    }

    // Combine multiple structured decisions into a result map
    public Map<String, Object> executeDecisionPipeline(String query, int gpa) {
        Map<String, Object> results = new HashMap<>();
        
        String policyCategory = evaluateChoicePolicy(query);
        boolean isEligible = evaluateBooleanEligibility("AT_LEAST", gpa, 3);
        int confidenceScore = calculateSupportScore(query, "gpa,scholarship,eligibility");

        results.put("policyCategory", policyCategory);
        results.put("isEligible", isEligible);
        results.put("confidenceScore", confidenceScore);

        return results;
    }
}
