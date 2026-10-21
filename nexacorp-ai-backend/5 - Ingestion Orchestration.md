**Ingestion Orchestration** means coordinating the ingestion pipelines for PDF, Wiki, and DB so that they ultimately produce a **common knowledge representation** and are stored in your chosen vector store(s) for RAG.

Think of it as:

```text
                 ┌── PDF Documents ──┐
                 │                   │
                 ├── Wiki Pages ─────┤
                 │                   │
                 └── Database ───────┘
                         │
                         ▼
               ┌───────────────────┐
               │ Ingestion         │
               │ Orchestrator      │
               └─────────┬─────────┘
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
      Extract         Clean/         Metadata
      content         Normalize       enrich
          │              │              │
          └──────────────┼──────────────┘
                         ▼
                    Chunking
                         │
                         ▼
                  Embedding Model
                         │
                         ▼
              ┌─────────────────────┐
              │ Vector Store        │
              │ e.g. PGVector       │
              └─────────────────────┘
                         │
                         ▼
                    RAG Retrieval
                         │
                         ▼
                       LLM
```

### What exactly is "orchestration"?

Suppose you have:

- **PDF** → product manuals
- **Wiki** → internal company knowledge
- **DB** → customer/product/application data

The individual ingestion pipelines might look like:

```text
PDF
 ↓
PDF Loader
 ↓
Document

Wiki
 ↓
Wiki/API Loader
 ↓
Document

DB
 ↓
JDBC/Repository
 ↓
Document
```

The **orchestrator** coordinates these three pipelines.

For example:

```java
@Service
public class IngestionOrchestrator {

    public void ingestAll() {

        List<Document> pdfDocs = pdfIngestionService.ingest();

        List<Document> wikiDocs = wikiIngestionService.ingest();

        List<Document> dbDocs = dbIngestionService.ingest();

        List<Document> allDocs = Stream.of(
                pdfDocs,
                wikiDocs,
                dbDocs
        )
        .flatMap(Collection::stream)
        .toList();

        List<Document> chunks = textSplitter.split(allDocs);

        vectorStore.add(chunks);
    }
}
```

The important point is that **the sources are different, but after extraction they can be converted into a common `Document` representation**.

For example:

```text
PDF
 └── Document
       content = "Mercedes vehicle telemetry..."
       metadata = {source=PDF, file=manual.pdf}

Wiki
 └── Document
       content = "Telemetry service architecture..."
       metadata = {source=WIKI, page=Telemetry}

DB
 └── Document
       content = "VIN: ABC123, model: C-Class..."
       metadata = {source=DB, table=vehicle}
```

Then:

```text
Document
   ↓
Text Splitter
   ↓
Chunks
   ↓
Embedding Model
   ↓
Vector Store
```

### Where Spring AI fits

Spring AI gives you abstractions for many of these pieces:

```text
              Spring AI
                  │
       ┌──────────┼──────────┐
       ▼          ▼          ▼
   Document    Embedding   VectorStore
   abstraction   Model       abstraction
       │          │          │
       ▼          ▼          ▼
     PDF/Wiki/DB → chunks → PGVector
```

For example, your application can ultimately do:

```java
vectorStore.add(documents);
```

Spring AI's `Document` abstraction helps normalize content coming from different sources.

You can then use a `VectorStore` such as PGVector.

---

## A realistic enterprise architecture

For your **PDF + Wiki + DB** use case, I would structure it like this:

```text
                 INGESTION ORCHESTRATOR
                         │
          ┌──────────────┼───────────────┐
          │              │               │
          ▼              ▼               ▼
    PDF Pipeline    Wiki Pipeline    DB Pipeline
          │              │               │
          ▼              ▼               ▼
     PDF Loader      Wiki API       JDBC/SQL
          │              │               │
          └──────────────┼───────────────┘
                         ▼
                 Normalization
                         │
                         ▼
                    Documents
                         │
                         ▼
                   Text Chunking
                         │
                         ▼
                  Metadata Enrichment
                         │
                         ▼
                  Embedding Model
                         │
                         ▼
                    PGVector
                         │
                         ▼
                     Retriever
                         │
                         ▼
                       RAG
```

### Why do we need orchestration?

Because ingestion is rarely just:

```java
vectorStore.add(pdf);
```

In a real system you need to coordinate things like:

**1. When should PDF ingestion run?**

```text
Every night
```

**2. When should Wiki ingestion run?**

```text
Every 1 hour
```

**3. When should DB ingestion run?**

```text
Whenever records change
```

**4. What happens if Wiki ingestion fails?**

You don't want PDF and DB ingestion to fail unnecessarily.

**5. How do we avoid duplicate data?**

For example:

```text
Wiki page #123
```

was already embedded yesterday.

**6. What happens when a PDF changes?**

You may need:

```text
Old chunks → delete/update
New PDF → chunk → embed → store
```

**7. How do we track the source?**

Metadata:

```json
{
  "source": "WIKI",
  "documentId": "wiki-123",
  "version": "5",
  "updatedAt": "2026-10-07"
}
```

---

# Important distinction

Don't confuse **Ingestion Orchestration** with **Embedding**.

They are different responsibilities.

### Ingestion

```text
PDF
Wiki
DB
 ↓
Extract
 ↓
Normalize
 ↓
Chunk
 ↓
Metadata
```

### Embedding

```text
Chunk
 ↓
Embedding Model
 ↓
[0.021, -0.183, 0.442, ...]
```

### Storage

```text
Embedding
+
Chunk text
+
Metadata
       ↓
   PGVector
```

So the complete pipeline is:

```text
        INGESTION ORCHESTRATION
                  │
       ┌──────────┼──────────┐
       ▼          ▼          ▼
      PDF        Wiki        DB
       │          │          │
       └──────────┼──────────┘
                  ▼
            Normalization
                  ▼
               Chunking
                  ▼
             Embeddings
                  ▼
              PGVector
```

And later, during a user's question:

```text
User Question
      │
      ▼
Embedding Model
      │
      ▼
Vector Search
      │
      ▼
PGVector
      │
      ▼
Relevant chunks
      │
      ▼
Prompt + Context
      │
      ▼
LLM
      │
      ▼
Answer
```

**That is the key RAG concept:** ingestion happens **before** the user asks a question; retrieval happens **when the user asks a question**.

In Spring AI, the orchestrator is usually **your application/service layer**, while Spring AI provides the reusable abstractions such as `Document`, `EmbeddingModel`, `VectorStore`, document readers/loaders, text splitting, etc. You don't need a special "IngestionOrchestrator" Spring AI class for this—the orchestration is an architectural pattern you build around Spring AI.

-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Exactly — **you can store the numerical vector in pgvector**, and in fact **that is the core purpose of pgvector**.

But in a RAG system, we usually **don't store only the vector**. We store the vector **along with the original chunk and metadata**.

### Think of one PDF chunk

Suppose your PDF contains:

> "Mercedes vehicles use telemetry data to monitor vehicle speed, battery status, and engine temperature."

After chunking:

```text
Chunk:
"Mercedes vehicles use telemetry data to monitor..."
```

The embedding model converts that text into something like:

```text
[0.021, -0.183, 0.442, 0.091, ..., 0.721]
```

That numerical array is the **embedding/vector**.

In PGVector, you might conceptually have:

| id | content | embedding | metadata |
|---|---|---|---|
| 101 | Mercedes vehicles use telemetry... | `[0.021,-0.183,...]` | `{source:"PDF", file:"manual.pdf", page:12}` |

### Why not store only this?

```text
[0.021, -0.183, 0.442, 0.091, ...]
```

Because the vector itself doesn't contain the actual text you want to give to the LLM.

When the user asks:

> "How does Mercedes telemetry monitor vehicle status?"

The process is:

```text
User question
      ↓
Embedding Model
      ↓
Question vector
      ↓
PGVector similarity search
      ↓
Find nearest vectors
      ↓
      101
      ↓
Retrieve the associated content
      ↓
"Mercedes vehicles use telemetry data..."
      ↓
Send this context to LLM
```

So the vector is used for **finding** the relevant information.

The original text is used for **giving the information to the LLM**.

---

## Think of PGVector as 3 pieces

```text
              PGVector
                 │
       ┌─────────┼─────────┐
       ▼         ▼         ▼
    Vector     Content   Metadata
      │           │          │
      │           │          │
 similarity     Actual      Source,
   search        text       page,
                           document
```

For example:

```sql
CREATE TABLE documents (
    id BIGSERIAL,
    content TEXT,
    embedding VECTOR(1536),
    metadata JSONB
);
```

Then:

```text
content
   ↓
"Mercedes telemetry monitors..."

embedding
   ↓
[0.021, -0.183, 0.442, ...]

metadata
   ↓
{
   "source": "PDF",
   "file": "vehicle-manual.pdf",
   "page": 12
}
```

### In Spring AI

This is why you generally work with a `Document`:

```java
Document document = new Document(
    "Mercedes vehicles use telemetry data...",
    Map.of(
        "source", "PDF",
        "file", "vehicle-manual.pdf",
        "page", 12
    )
);
```

Then:

```java
vectorStore.add(List.of(document));
```

Spring AI handles the process of generating/storing the embedding through the configured embedding model/vector store integration.

So **you don't normally manually create and insert the numerical vector yourself**.

---

### One very important distinction

**PGVector doesn't create the embedding.**

The architecture is:

```text
                 Embedding Model
                       │
                       │ creates
                       ▼
              Numerical Vector
                       │
                       ▼
                    PGVector
                       │
             stores + searches
                       │
                       ▼
                 Similar chunks
```

So:

> **Embedding Model = converts text → numbers**

> **PGVector = stores those numbers + performs similarity search**

> **Document/metadata = lets you retrieve the actual source information**

This is also why your earlier **PDF + Wiki + DB ingestion orchestration** ultimately converges into the same structure: regardless of where the information came from, each chunk can become **content + embedding + metadata** in the vector store.