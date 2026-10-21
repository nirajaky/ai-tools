# Spring AI + RAG
## Document Ingestion, Embeddings, pgvector & Redis — Complete Notes

---

## 1. What is RAG?

**RAG = Retrieval-Augmented Generation.**

RAG allows an LLM to answer questions using information from your own data/documents.

Instead of relying only on the knowledge already available to the LLM:

```text
User Question
      ↓
Retrieve relevant information
      ↓
Give information to LLM as context
      ↓
LLM generates answer
```

### Basic RAG architecture

```text
                 YOUR KNOWLEDGE
                       │
        PDF / DOC / Website / Database
                       │
                       ↓
                  INGESTION
                       │
                       ↓
               Vector Database
                       │
                       │
                       ▼
User Question ──→ RETRIEVAL
                       │
                       ↓
                Relevant Chunks
                       │
                       ↓
                    LLM
                       │
                       ↓
                  Final Answer
```

---

# 2. What is Ingestion?

In the context of Spring AI and RAG:

> **Ingestion is the process of taking raw documents/data, extracting their content, splitting the content into manageable chunks, converting those chunks into embeddings, and storing the chunks and embeddings in a vector store for future retrieval.**

In simple words:

> **Ingestion prepares your documents so that an AI application can search them semantically later.**

---

# 3. Why do we need ingestion?

Suppose you have a company policy PDF:

```text
employee-policy.pdf
```

It contains 100 pages.

A user later asks:

```text
How many annual leaves can an employee take?
```

The LLM shouldn't necessarily receive the entire 100-page PDF.

Instead, during ingestion, we prepare the PDF:

```text
100-page PDF
     ↓
Extract text
     ↓
Split into chunks
     ↓
Generate embeddings
     ↓
Store in vector database
```

Later, only the relevant chunks are retrieved.

---

# 4. Complete Ingestion Pipeline

The typical ingestion pipeline is:

```text
PDF / DOC / Website / DB
          ↓
    Document Reader
          ↓
      Documents
          ↓
      Text Splitter
          ↓
        Chunks
          ↓
    Embedding Model
          ↓
      Embeddings
          ↓
     Vector Store
          ↓
PostgreSQL + pgvector
```

---

# 5. Step 1 — Document Loading

First, the application needs to read the original document.

For example:

```text
employee-policy.pdf
```

The PDF contains:

```text
Employees are eligible for 20 days of annual leave...
```

A document reader extracts this information.

Conceptually:

```java
Document document = ...
```

Spring AI provides abstractions for working with documents and document readers.

The result is generally represented as a `Document`.

A `Document` can contain:

```text
Content
Metadata
```

For example:

```text
Content:
"Employees are eligible for 20 days of annual leave."

Metadata:
source = employee-policy.pdf
page = 15
```

---

# 6. Step 2 — Document Splitting / Chunking

A large document should normally not be stored as one huge piece.

Suppose:

```text
100-page PDF
```

Instead:

```text
PDF
 ↓
Chunk 1
Chunk 2
Chunk 3
Chunk 4
...
Chunk 500
```

For example:

```text
Chunk 1:
Employees are eligible for annual leave...

Chunk 2:
Employees must apply for leave through...

Chunk 3:
Carry-forward of annual leave is limited to...
```

### Why chunk documents?

Because RAG needs to retrieve **relevant pieces of information**, not necessarily the entire document.

If chunks are too large:

- Retrieval may return unnecessary information.
- More tokens may be sent to the LLM.
- Search precision may decrease.

If chunks are too small:

- Important context can be lost.
- A sentence may be separated from the information required to understand it.

Therefore, chunk size and overlap are important design considerations.

---

# 7. Spring AI Text Splitting

Spring AI provides text-splitting utilities.

One example is:

```java
TokenTextSplitter
```

Conceptually:

```java
List<Document> documents = reader.get();

List<Document> chunks =
        textSplitter.apply(documents);
```

The exact implementation can vary depending on your Spring AI version and application architecture.

---

# 8. Step 3 — Embedding Model

This is one of the most important concepts in RAG.

An **Embedding Model** converts text into a numerical vector.

For example:

```text
"Employees are eligible for 20 days of annual leave."
```

might become something conceptually like:

```text
[0.21, -0.14, 0.87, 0.31, ...]
```

The actual vector can contain hundreds or thousands of dimensions depending on the embedding model.

The vector represents the **semantic meaning** of the text.

---

# 9. Why Embeddings?

Traditional keyword search looks for matching words.

For example:

```text
Question:
How many vacation days do employees receive?
```

Document:

```text
Employees are eligible for 20 days of annual leave.
```

The words aren't exactly identical:

```text
vacation ≠ annual leave
receive ≠ eligible
days ≈ days
```

But semantically they mean similar things.

Embeddings allow the system to represent the meaning mathematically.

Therefore:

```text
"vacation days"
```

and

```text
"annual leave"
```

can have vectors that are relatively close in vector space.

---

# 10. Very Important — When is the Embedding Model Used?

The embedding model is **not used only during ingestion**.

It is normally used in **two important stages**.

## A. During ingestion

Document chunk:

```text
"Employees are eligible for 20 days of annual leave."
```

↓

Embedding Model

↓

```text
[0.21, -0.14, 0.87, ...]
```

↓

Store vector in vector database.

---

## B. During retrieval

Suppose the user asks:

```text
How many annual leaves do employees get?
```

The question is also converted into a vector:

```text
User Question
      ↓
Embedding Model
      ↓
Query Vector
```

Then the vector database searches for document vectors that are semantically similar to the query vector.

Therefore:

```text
INGESTION:

Document Chunk
      ↓
Embedding Model
      ↓
Document Vector
      ↓
Vector DB


RETRIEVAL:

User Question
      ↓
Embedding Model
      ↓
Query Vector
      ↓
Vector DB
      ↓
Similar Document Vectors
```

### Key point

> **The embedding model is used both when indexing documents and when searching for relevant documents.**

---

# 11. Embedding Model vs Chat Model

This distinction is extremely important in Spring AI.

## EmbeddingModel

Purpose:

```text
Text → Vector
```

Used for:

- Document embeddings
- Query embeddings
- Semantic search

It does **not** normally generate the final natural-language answer.

---

## ChatModel / LLM

Purpose:

```text
Prompt + Context
       ↓
Natural-language answer
```

For example:

```text
User Question
+
Retrieved Context
        ↓
      ChatModel
        ↓
Final Answer
```

### Remember

```text
EmbeddingModel → creates vectors

ChatModel → generates answers
```

---

# 12. Step 4 — Vector Database

After generating embeddings, we need somewhere to store them.

That is the job of a **Vector Store / Vector Database**.

Conceptually, each stored record may contain:

```text
Document ID
Chunk text
Embedding vector
Metadata
```

Example:

```text
ID:
101

Text:
Employees are eligible for 20 days of annual leave.

Embedding:
[0.21, -0.14, 0.87, ...]

Metadata:
source = employee-policy.pdf
page = 15
department = HR
```

---

# 13. PostgreSQL + pgvector

PostgreSQL can be extended with **pgvector** to store and search vectors.

So PostgreSQL can handle both:

```text
Traditional relational data
+
Vector data
```

For example:

```text
PostgreSQL
│
├── users
├── employees
├── orders
├── products
│
└── RAG data
     ├── documents
     ├── chunks
     ├── embeddings
     └── metadata
```

This can be very convenient if your application already uses PostgreSQL.

---

# 14. Is PostgreSQL itself a Vector Database?

More precisely:

> **PostgreSQL is a relational database, and pgvector adds vector storage and vector similarity-search capabilities to PostgreSQL.**

So when we say:

```text
PostgreSQL + pgvector
```

we mean PostgreSQL is being used as a vector store as well.

---

# 15. Redis and RAG

Redis can also support vector search.

Therefore, Redis can technically be used as a vector database/vector store.

For example:

```text
Documents
    ↓
EmbeddingModel
    ↓
Redis Vector Store
    ↓
Vector Search
    ↓
Relevant chunks
    ↓
LLM
```

However, this does **not** mean that every Redis deployment should be used to store your RAG knowledge base.

---

# 16. Your Important Scenario — Redis is Temporary

Suppose your system has:

```text
PostgreSQL + pgvector
Redis
```

and Redis is being used as a **temporary/cache database**.

Then the ideal architecture is:

```text
PostgreSQL + pgvector
        ↓
Permanent RAG Knowledge Base


Redis
        ↓
Temporary Cache
```

In this architecture, **Redis should not be the source of truth for your RAG knowledge base.**

---

# 17. Where Should Ingestion Data Be Stored?

For your scenario:

```text
                 DOCUMENT
                    ↓
                Ingestion
                    ↓
              EmbeddingModel
                    ↓
        PostgreSQL + pgvector
                    ↓
          Permanent Knowledge Base
```

Redis does not need to store the permanent RAG data.

---

# 18. What About the Original PDF?

An important architectural distinction:

The original PDF does not necessarily need to be stored inside PostgreSQL.

A production architecture could use:

```text
Azure Blob Storage / S3
        ↓
Original PDF
```

and:

```text
PostgreSQL + pgvector
        ↓
Chunks
Embeddings
Metadata
```

So:

```text
                Original Document
                       │
                       ↓
              Azure Blob / S3
                       │
                       │
                       ↓
                 Spring AI
                       │
                       ↓
                   Chunking
                       │
                       ↓
                EmbeddingModel
                       │
                       ↓
             PostgreSQL + pgvector
                       │
             ┌─────────┴─────────┐
             │                   │
          Chunks             Embeddings
             │                   │
             └─────────┬─────────┘
                       ↓
                RAG Knowledge Base
```

---

# 19. What Should Redis Store?

If Redis is your temporary/cache layer, it can store things such as:

```text
Frequently requested answers
```

or:

```text
Question → Answer
```

or potentially:

```text
Question → Retrieved context
```

For example:

```text
User:
What is the leave policy?

        ↓

Redis
        ↓
Cache HIT?
```

If yes:

```text
Return cached result
```

If no:

```text
Redis
  ↓
Cache MISS
  ↓
PostgreSQL + pgvector
  ↓
Similarity Search
  ↓
Relevant Chunks
  ↓
LLM
  ↓
Answer
  ↓
Store result temporarily in Redis
```

---

# 20. Why Use Redis as Cache?

Redis is commonly used because it provides very fast access to frequently needed data.

Example:

```text
                User Question
                     ↓
                   Redis
                     ↓
                Cache HIT?
                 /       \
               YES        NO
                ↓          ↓
             Answer    pgvector
                           ↓
                       Retrieval
                           ↓
                          LLM
                           ↓
                        Answer
                           ↓
                        Redis
```

If Redis is restarted or its cache is cleared:

```text
Redis ❌
```

but:

```text
PostgreSQL + pgvector ✅
```

still contains the permanent knowledge base.

The application can perform the vector search again and recreate the cache.

---

# 21. Redis vs pgvector for Your Architecture

| Requirement | PostgreSQL + pgvector | Redis |
|---|---|---|
| Relational application data | ✅ Excellent | ⚠️ Not primary purpose |
| Permanent RAG knowledge | ✅ Good choice | ✅ Technically possible |
| Vector search | ✅ Yes | ✅ Yes |
| Temporary caching | Possible | ✅ Excellent |
| Fast cache access | Good | ✅ Excellent |
| Source of truth | ✅ Suitable | Usually not for temporary Redis |
| Original documents | Usually use Blob/S3 instead | Usually not |
| RAG chunks | ✅ Suitable | Possible |
| Embeddings | ✅ Suitable | Possible |

### For your system

A clean choice is:

```text
PostgreSQL + pgvector → Permanent RAG data

Redis → Cache / temporary data
```

---

# 22. Complete Production-Style Architecture

A good conceptual architecture for your Spring AI application is:

```text
                     ┌───────────────────┐
                     │ PDF / DOC / Files │
                     └─────────┬─────────┘
                               ↓
                     ┌───────────────────┐
                     │ Azure Blob / S3   │
                     │ Original Documents│
                     └─────────┬─────────┘
                               ↓
                         Spring AI
                               ↓
                       Document Reader
                               ↓
                          Text Splitter
                               ↓
                             Chunks
                               ↓
                        EmbeddingModel
                               ↓
                  ┌─────────────────────────┐
                  │ PostgreSQL + pgvector   │
                  │                         │
                  │ Chunks                  │
                  │ Embeddings              │
                  │ Metadata                │
                  └────────────┬────────────┘
                               ↓
                         Vector Search
                               ↓
                        Relevant Chunks
                               ↓
                         ChatClient
                               ↓
                           ChatModel
                               ↓
                             LLM
                               ↓
                          Final Answer
                               │
                               ↓
                            Redis
                            Cache
```

---

# 23. Ingestion vs Retrieval

This is one of the most important distinctions to remember.

## Ingestion

Purpose:

> **Prepare and index knowledge.**

```text
Document
   ↓
Read
   ↓
Split
   ↓
Embed
   ↓
Store
```

Usually performed:

- When a new document is uploaded
- When an existing document changes
- During initial knowledge-base creation
- During scheduled re-indexing

---

## Retrieval

Purpose:

> **Find the relevant knowledge for a user's question.**

```text
Question
   ↓
Embed Question
   ↓
Vector Search
   ↓
Relevant Chunks
```

This happens when the user asks a question.

---

# 24. Ingestion vs Retrieval vs Generation

Think of RAG as three major stages:

```text
1. INGESTION
Document → Chunks → Embeddings → Vector Store


2. RETRIEVAL
Question → Embedding → Vector Search → Relevant Chunks


3. GENERATION
Question + Relevant Chunks → LLM → Answer
```

### Easy memory trick

> **Ingestion prepares. Retrieval finds. Generation answers.**

---

# 25. Spring AI Conceptual Mapping

The Spring AI concepts you should understand are:

```text
Document
   ↓
Document Reader
   ↓
Text Splitter
   ↓
EmbeddingModel
   ↓
VectorStore
   ↓
ChatClient
   ↓
ChatModel
```

A simplified ingestion pipeline might look conceptually like:

```java
List<Document> documents = reader.get();

List<Document> chunks =
        textSplitter.apply(documents);

vectorStore.add(chunks);
```

Behind the scenes, the vector store can work with an embedding model to convert the documents/chunks into vectors and persist them.

---

# 26. Spring AI `EmbeddingModel`

Conceptually:

```java
EmbeddingModel
```

is responsible for generating embeddings.

Think:

```text
EmbeddingModel

String
  ↓
Vector
```

For example:

```text
"Annual leave is 20 days."
        ↓
EmbeddingModel
        ↓
[0.12, 0.45, -0.22, ...]
```

---

# 27. Spring AI `VectorStore`

Think of `VectorStore` as the abstraction that allows the application to work with a vector database.

Conceptually:

```text
Application
     ↓
Spring AI VectorStore
     ↓
PostgreSQL + pgvector
```

or potentially:

```text
Application
     ↓
Spring AI VectorStore
     ↓
Redis
```

The underlying implementation can differ.

This abstraction helps prevent your application code from becoming tightly coupled to one vector database.

---

# 28. Retrieval Process in Detail

Suppose the vector database contains:

```text
Chunk A:
Annual leave is 20 days.

Chunk B:
Employees must submit leave requests through HR portal.

Chunk C:
Work-from-home requests require manager approval.
```

User asks:

```text
How many annual leaves are available?
```

The process is:

```text
User Question
      ↓
EmbeddingModel
      ↓
Query Vector
      ↓
VectorStore
      ↓
Similarity Search
      ↓
Chunk A
      ↓
ChatClient / ChatModel
      ↓
Answer:
"Employees are eligible for 20 days of annual leave."
```

---

# 29. Why the Same Embedding Model Matters

During ingestion:

```text
Document Chunk
      ↓
EmbeddingModel
      ↓
Document Vector
```

During retrieval:

```text
User Question
      ↓
EmbeddingModel
      ↓
Query Vector
```

The document and query vectors need to be generated in a compatible embedding space.

Therefore, normally you use the same embedding model/configuration for both document ingestion and query embedding.

---

# 30. Important Mental Model

Don't think:

```text
PDF → LLM
```

Think:

```text
PDF
 ↓
INGESTION
 ↓
Knowledge Base
 ↓
User Question
 ↓
RETRIEVAL
 ↓
Relevant Knowledge
 ↓
LLM
 ↓
Answer
```

---

# 31. Example End-to-End Scenario

Imagine your company has:

```text
HR Policy.pdf
Insurance Policy.pdf
Employee Handbook.pdf
Leave Policy.pdf
```

### During ingestion

```text
PDF files
    ↓
Document Reader
    ↓
Extract text
    ↓
Split into chunks
    ↓
EmbeddingModel
    ↓
Vectors
    ↓
PostgreSQL + pgvector
```

The database now contains searchable knowledge.

### Later, employee asks:

```text
How many casual leaves do I get?
```

### Retrieval

```text
Question
   ↓
EmbeddingModel
   ↓
Query Vector
   ↓
pgvector similarity search
   ↓
Relevant Leave Policy chunks
```

### Generation

```text
User Question
+
Retrieved Leave Policy chunks
        ↓
     ChatClient
        ↓
     ChatModel/LLM
        ↓
"According to the leave policy, ..."
```

---

# 32. Where Redis Fits

If Redis is being used as a temporary cache:

```text
                         ┌───────────────┐
                         │    Redis      │
                         │    Cache      │
                         └───────▲───────┘
                                 │
                                 │ Cache
                                 │
User → Spring Boot → RAG → LLM
                  │
                  ↓
           PostgreSQL
            + pgvector
                  │
                  ↓
          Permanent RAG KB
```

Redis is an **optimization layer**, not necessarily the knowledge base.

---

# 33. Very Important Interview Question

### Q: Is the embedding model used only during document ingestion?

### Answer:

**No.**

It is typically used both during:

1. **Document ingestion** — to convert document chunks into vectors.
2. **Query retrieval** — to convert the user's question into a vector so that similarity search can find relevant document chunks.

The **ChatModel/LLM** is responsible for generating the final natural-language answer.

---

# 34. Another Interview Question

### Q: If Redis and PostgreSQL are both present, where should RAG embeddings be stored?

### Answer:

It depends on the architecture.

If Redis is being used only as a temporary cache, then:

```text
PostgreSQL + pgvector
        ↓
Permanent RAG embeddings
```

and:

```text
Redis
 ↓
Temporary cache
```

Redis can technically act as a vector store, but there is no reason to use it as the permanent knowledge base if your architecture already uses PostgreSQL + pgvector for persistent vector storage.

---

# 35. Another Important Interview Question

### Q: Where should the original PDF be stored?

A common production approach is:

```text
Original PDF
     ↓
Azure Blob Storage / S3
```

while:

```text
Chunks
Embeddings
Metadata
     ↓
PostgreSQL + pgvector
```

This separates:

**Document storage** from **semantic-search storage**.

---

# 36. Final Architecture to Remember

For your Spring AI + RAG learning project, keep this architecture in mind:

```text
                 ┌──────────────────┐
                 │ Original Documents│
                 └────────┬─────────┘
                          ↓
                    Document Reader
                          ↓
                     Text Splitter
                          ↓
                        Chunks
                          ↓
                   EmbeddingModel
                          ↓
              ┌───────────────────────┐
              │ PostgreSQL + pgvector │
              │                       │
              │ RAG Knowledge Base    │
              └───────────┬───────────┘
                          ↓
                    Vector Search
                          ↓
                    Relevant Chunks
                          ↓
                       ChatClient
                          ↓
                       ChatModel
                          ↓
                          LLM
                          ↓
                     Final Answer

              Redis
                 ↑
                 │
          Optional Cache
```

---

# 37. Quick Revision Sheet

### RAG

```text
Retrieval-Augmented Generation
```

### Ingestion

```text
Raw data → processed/indexed knowledge
```

### Chunking

```text
Large document → smaller pieces
```

### Embedding

```text
Text → numerical vector
```

### EmbeddingModel

```text
Creates embeddings
```

### VectorStore

```text
Stores/searches vectors
```

### pgvector

```text
PostgreSQL extension for vector storage/search
```

### Redis

```text
Can be a vector store OR cache
```

But if your Redis is configured as temporary cache:

```text
Redis → Cache
PostgreSQL + pgvector → Permanent RAG KB
```

### Retrieval

```text
Question → Query Embedding → Similarity Search → Relevant Chunks
```

### Generation

```text
Question + Relevant Chunks → LLM → Answer
```

---

# 38. The Most Important 5 Lines

If you remember only five things, remember these:

```text
1. Ingestion prepares documents for RAG.

2. EmbeddingModel converts text into vectors.

3. EmbeddingModel is used during both ingestion and retrieval.

4. PostgreSQL + pgvector can be the permanent RAG knowledge base.

5. Redis can be used as a temporary cache; it does not have to store the permanent knowledge base.
```

### One-line mental model

> **Ingestion creates the searchable knowledge base; retrieval finds the relevant knowledge; the LLM uses that knowledge to generate the answer.**