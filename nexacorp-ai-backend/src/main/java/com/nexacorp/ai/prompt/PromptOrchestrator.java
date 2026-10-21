package com.nexacorp.ai.prompt;

import com.nexacorp.ai.prompt.model.ChatPrompt;
import com.nexacorp.ai.prompt.model.PromptContext;
import com.nexacorp.ai.prompt.model.SystemInstructions;
import com.nexacorp.ai.retrieval.RetrievalService;
import com.nexacorp.ai.retrieval.model.RetrievalResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromptOrchestrator {

    private final RetrievalService retrievalService;
    private final ContextBuilder contextBuilder = new ContextBuilder();
    private final SystemPromptLoader systemPromptLoader = new SystemPromptLoader();
    private final GroundingPolicy groundingPolicy = new GroundingPolicy();

    public ChatPrompt build(String question) {

        RetrievalResult retrievalResult = retrievalService.retrieve(question);

        PromptContext promptContext = contextBuilder.build(retrievalResult);

        String rule = groundingPolicy.groundingRules(promptContext);

        SystemInstructions systemInstructions = systemPromptLoader.load();

        return new ChatPrompt(systemInstructions, promptContext,  rule);
    }
}
