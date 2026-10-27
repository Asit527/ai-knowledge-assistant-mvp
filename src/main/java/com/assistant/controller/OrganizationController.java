package com.assistant.controller;

import com.assistant.model.Organization;
import com.assistant.model.Assistant;
import com.assistant.repository.OrganizationRepository;
import com.assistant.repository.AssistantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class OrganizationController {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AssistantRepository assistantRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerOrganization(@RequestParam String name) {
        if (organizationRepository.findByName(name).isPresent()) {
            return ResponseEntity.badRequest().body("Organization already exists!");
        }
        
        Organization org = new Organization();
        org.setName(name);
        Organization savedOrg = organizationRepository.save(org);

        // Automatically create a unique assistant token for this organization
        Assistant assistant = new Assistant();
        assistant.setOrganization(savedOrg);
        assistantRepository.save(assistant);

        return ResponseEntity.ok(savedOrg);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginOrganization(@RequestParam String name) {
        Optional<Organization> org = organizationRepository.findByName(name);
        if (org.isPresent()) {
            Optional<Assistant> assistant = assistantRepository.findByOrganizationId(org.get().getId());
            return ResponseEntity.ok("Login successful! Assistant Token: " + assistant.get().getToken());
        }
        return ResponseEntity.status(401).body("Organization not found!");
    }
}
