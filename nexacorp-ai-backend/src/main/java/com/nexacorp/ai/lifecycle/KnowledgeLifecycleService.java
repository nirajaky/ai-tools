package com.nexacorp.ai.lifecycle;

import com.nexacorp.ai.lifecycle.model.KnowledgeRequest;
import com.nexacorp.ai.vectorstore.ChunkVectorStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgeLifecycleService {
    private final ChunkVectorStoreService vectorStoreService;

    public void ingest(KnowledgeRequest request) {

        String identity = KnowledgeIdentity.from(request);

        // TODO: to be implemented
    }

    public void delete(KnowledgeRequest request) {
        String identity = KnowledgeIdentity.from(request);
        vectorStoreService.deleteByIdentity(identity);
    }

    public void deleteAll() {
        vectorStoreService.deleteAll();
    }

}
