package com.assistant.config;

import com.assistant.model.Assistant;
import com.assistant.model.Organization;
import com.assistant.repository.AssistantRepository;
import com.assistant.repository.OrganizationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(OrganizationRepository orgRepo, AssistantRepository assistantRepo) {
        return args -> {
            // Create default test organization if not exists
            if (orgRepo.findByName("TechCorp University").isEmpty()) {
                Organization org = new Organization();
                org.setName("TechCorp University");
                Organization savedOrg = orgRepo.save(org);

                Assistant assistant = new Assistant();
                assistant.setOrganization(savedOrg);
                assistant.setToken("test-token-techcorp-123");
                assistantRepo.save(assistant);
                
                System.out.println("Initialized default test Organization 'TechCorp University' with token: test-token-techcorp-123");
            }
        };
    }
}
