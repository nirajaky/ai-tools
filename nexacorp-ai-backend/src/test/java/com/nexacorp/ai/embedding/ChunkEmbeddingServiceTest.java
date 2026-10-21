package com.nexacorp.ai.embedding;

import com.nexacorp.ai.chunking.ChunkingOrchestrator;
import com.nexacorp.ai.chunking.ChunkingOrchestratorTest;
import com.nexacorp.ai.chunking.model.Chunk;
import com.nexacorp.ai.embedding.model.EmbeddedChunk;
import com.nexacorp.ai.ingestion.IngestionOrchestrator;
import com.nexacorp.ai.ingestion.model.IngestedDocument;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ChunkEmbeddingServiceTest {
    private static final Logger log = LoggerFactory.getLogger(ChunkEmbeddingServiceTest.class);

    @Autowired
    private IngestionOrchestrator ingestionOrchestrator;

    @Autowired
    private ChunkingOrchestrator chunkingOrchestrator;

    @Autowired
    private ChunkEmbeddingService chunkEmbeddingService;

    @Test
    void testEmbedding() throws Exception {
        List<IngestedDocument> documents = ingestionOrchestrator.ingestAll();
        IngestedDocument document = documents.get(0);

        List<Chunk> chunks = chunkingOrchestrator.chunk(document);
        for (Chunk chunk : chunks) {
            EmbeddedChunk embedded = chunkEmbeddingService.embed(chunk);

            log.info("Metadata    : {}", chunk.getMetadata());
            log.info("Content     : {}", chunk.getContent());
            log.info("Embedding length : {}", embedded.getVector().length );
            log.info("Embedding Vector : {}", embedded.getVector() );
        }
    }
}
