package com.nexacorp.ai.vectorstore;

import com.nexacorp.ai.chunking.model.Chunk;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChunkVectorStoreService {

    private final VectorStore vectorStore;

    public ChunkVectorStoreService(@Qualifier("customVectorStore") VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void store(List<Chunk> chunks) {

        List<Document> documents = chunks.stream().map(chunk -> {
            Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
            metadata.put("source", chunk.getSource());
            metadata.put("chunkIndex", chunk.getChunkIndex());

            return new Document(
                    chunk.getContent(),
                    metadata
            );
        }).collect(Collectors.toList());

        vectorStore.add(documents);
    }

    public void deleteAll() {
        FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
        Filter.Expression filter = filterBuilder.gte("chunkIndex", -1).build();
        SearchRequest searchRequest = SearchRequest.builder().query(" ")
                .filterExpression(filter)
                .topK(10)
                .build();
        while (!vectorStore.similaritySearch(searchRequest).isEmpty()) {
            vectorStore.delete(filter);
        }
    }

    public void deleteByIdentity(String identity) {
        FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
        vectorStore.delete(filterBuilder.eq("identity", identity).build());
    }
}
