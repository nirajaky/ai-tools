package com.nexacorp.ai.lifecycle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class KnowledgeLifecycleServiceTest {
    @Autowired
    private KnowledgeLifecycleService knowledgeLifecycleService;

    @Test
    void testDeleteAll() {
        knowledgeLifecycleService.deleteAll();
    }
}
