## 1. What is Retrieval in Spring AI and RAG?

Retrieval means finding the most relevant information from your vector database based on a user's question.

Suppose you store PDFs and wiki documents in PostgreSQL with pgvector or Redis.

The flow looks like this:

1\. User asks a question

"How does our payment service work?"

2\. Convert question into an embedding

Spring AI's embedding model converts text into a vector.

3\. Retrieve relevant chunks

Search pgvector or Redis for similar vectors.

4\. Send retrieved chunks to the LLM

The LLM generates an answer using the retrieved context.

In Spring AI, you commonly use a `VectorStore` and `QuestionAnswerAdvisor` or `RetrievalAugmentationAdvisor` to help implement retrieval and RAG.

## 2. What are `vector_score` and `distance`?

Both help measure how closely a stored document chunk matches the user's question.

Imagine your question is: "How does payment processing work?"

Your vector database finds three chunks:

| Chunk                     | Distance | Similarity       |
| ------------------------- | -------- | ---------------- |
| Payment processing guide  | 0.10     | Very similar     |
| Payment API documentation | 0.25     | Similar          |
| Employee leave policy     | 0.85     | Not very similar |

These are illustrative values; their meaning depends on the distance metric.

- Distance: Measures how far apart two vectors are. With common distance metrics, a lower distance means more similar.
- Vector score: Often means a relevance or similarity score assigned to a result. Depending on the database or API, a higher score may mean more similar, or the score may represent distance itself.

Important: `vector_score` is not a universal field with one fixed meaning. Check the particular Spring AI version, vector store, and distance metric to understand how it is calculated.

## 3. How does retrieval choose the chunks?

You can configure retrieval to return the top `K` results.

For example:

```
SearchRequest.builder()
    .query("How does payment processing work?")
    .topK(3)
    .build();
```

This requests the three most relevant chunks. You can also apply a similarity threshold to filter out weak matches, depending on the vector store and Spring AI API.

Remember:

- Retrieval = finding relevant chunks.
- Distance = how far apart vectors are.
- Similarity score = how closely vectors match, depending on the scoring method.
- Top-K = maximum number of results requested.