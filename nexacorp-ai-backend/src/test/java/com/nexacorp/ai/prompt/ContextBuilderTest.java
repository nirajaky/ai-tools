package com.nexacorp.ai.prompt;

import com.nexacorp.ai.prompt.model.PromptContext;
import com.nexacorp.ai.retrieval.RetrievalService;
import com.nexacorp.ai.retrieval.model.RetrievalResult;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ContextBuilderTest {
    private static final Logger log = LoggerFactory.getLogger(ContextBuilderTest.class);

    @Autowired
    private RetrievalService  retrievalService;

    @Test
    void testContextBuilder() {
        RetrievalResult result = retrievalService.retrieve("What is the work from home policy");

        PromptContext promptContext = new ContextBuilder().build(result);

        log.info("=== Loaded Context ===");
        log.info("\n{}", promptContext.getContextText());
    }
}
