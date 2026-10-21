package com.nexacorp.ai.lifecycle;

import com.nexacorp.ai.lifecycle.model.KnowledgeRequest;
import com.nexacorp.ai.lifecycle.model.SourceType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class KnowledgeIdentityTest {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIdentityTest.class);

    @Test
    void testSourceIdentity() {
        KnowledgeRequest request = KnowledgeRequest.builder()
                .sourceType(SourceType.PDF)
                .name("HR_Leave_Policy.pdf")
                .build();
        log.info("Identity = {}", KnowledgeIdentity.from(request));

        KnowledgeRequest dbRequest = KnowledgeRequest.builder()
                .sourceType(SourceType.DATABASE)
                .name("faqs")
                .build();
        log.info("Identity = {}", KnowledgeIdentity.from(dbRequest));

    }
}
