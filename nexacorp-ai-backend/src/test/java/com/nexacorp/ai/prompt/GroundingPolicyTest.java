package com.nexacorp.ai.prompt;

import com.nexacorp.ai.prompt.model.PromptContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GroundingPolicyTest {

    private static final Logger log = LoggerFactory.getLogger(GroundingPolicyTest.class);

    @Test
    void emptyContextTest() {
        GroundingPolicy policy = new GroundingPolicy();
        String rule = policy.groundingRules(new PromptContext(""));
        log.info("=== EMPTY CONTEXT RULES ===");
        log.info("\n{}", rule);
    }

    @Test
    void nonEmptyContextTest() {
        GroundingPolicy policy = new GroundingPolicy();
        String rule = policy.groundingRules(new PromptContext("Some internal policy text"));
        log.info("=== GROUNDED CONTEXT RULES ===");
        log.info("\n{}", rule);
    }
}
