# 🎬 MovieMatch — Semantic Movie Recommendation System

A **Spring Boot** application that recommends movies using **semantic search** powered by **embeddings** and a **vector database** (Pinecone).

Instead of keyword matching, MovieMatch embeds your search query and movie descriptions, then finds movies whose **meaning** is closest to your request.

---

## ✨ Features

- **Semantic search** — describe the kind of movie you want, get the closest matches
- **"Similar movies"** — click any movie to find semantically similar ones
- **Google Gemini embeddings** — `gemini-embedding-001` (768 dimensions)
- **Pinecone vector store** — embeddings are permanently stored and queried
- **Clean frontend** — static HTML/CSS/JS served by Spring Boot

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| AI / Embeddings | Spring AI 2.0.2 + Google Gemini |
| Vector Database | Pinecone |
| Frontend | HTML / CSS / JavaScript (served from `src/main/resources/static`) |
| Build | Maven |

---

## 📁 Project Structure

```
netflix-recommender/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/netflix/recommender/
    │   ├── RecommenderApplication.java      # Spring Boot entry point
    │   ├── controller/
    │   │   └── MoviesController.java        # REST API (@CrossOrigin)
    │   ├── service/
    │   │   └── MovieService.java            # Seeding + similarity search logic
    │   └── model/
    │       ├── MovieData.java               # Pojo for movies.json entries
    │       ├── Movie.java                   # Movie model (legacy/in-memory)
    │       └── MovieMatch.java              # API response object (title, desc, score)
    └── resources/
        ├── application.properties            # Config (uses env vars)
        ├── movies.json                       # Seed data (titles + descriptions)
        └── static/
            └── index.html                    # Frontend UI
```

---

## 🔌 Prerequisites

1. **Java 21** installed
2. **Maven** (or use the bundled `mvnw.cmd` wrapper)
3. **Google Gemini API key**
   - Get one at [Google AI Studio](https://aistudio.google.com/)
   - Used for both chat model and embeddings
4. **Pinecone account + index**
   - Sign up at [pinecone.io](https://www.pinecone.io/)
   - Create an index:
     - **Name:** `movie-recommender`
     - **Dimensions:** `768`
     - **Metric:** `cosine`
   - Copy your **API key** from Console → API Keys (UUID format, e.g. `123e4567-e89b-12d3-a456-426614174000`)

---

## ⚙️ Configuration

All secrets are read from **environment variables** (never hardcoded). Set these before running:

| Variable | Description |
|----------|-------------|
| `GEMINI_API_KEY` | Your Google Gemini API key |
| `PINECONE_API_KEY` | Your Pinecone API key (UUID format) |

### Set them (PowerShell)

```powershell
$env:GEMINI_API_KEY="your-gemini-key"
$env:PINECONE_API_KEY="your-pinecone-uuid-key"
```

> **Note:** These only live in that terminal window. For permanent setup, use `setx` and restart your terminal/IDE.

---

## 🚀 Running the App

```powershell
# From the project root
mvnw.cmd spring-boot:run
```

Or run `RecommenderApplication.java` directly from IntelliJ.

Once started, open: **http://localhost:8080/**

---

## 🔄 How Seeding Works

On **first startup**, `MovieService`:
1. Loads `movies.json` (title + description for each movie)
2. Embeds every description using Gemini
3. Permanently stores the vectors in Pinecone

On **later startups**, it detects the store is already seeded and **skips re-embedding** (no repeat API cost or uploads).

---

## 🔗 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/movies/search?query=...` | Semantic search — returns top 4 matching movies |
| GET | `/movies/{title}/similar` | Returns movies similar to the given title |

**Example response:**
```json
[
  {
    "title": "The Matrix",
    "description": "A hacker discovers that reality is a simulated world...",
    "match": 0.599
  }
]
```

CORS is enabled via `@CrossOrigin(origins = "*")` on the controller.

---

## ❗ Troubleshooting

| Problem | Fix |
|---------|-----|
| `401 UNAUTHENTICATED / Invalid API key` | Your Pinecone key is wrong/partial. Use the full UUID from the console. |
| App fails to start, "no API key" | Set `GEMINI_API_KEY` and `PINECONE_API_KEY` env vars before running |
| Port already in use | Another instance is running on 8080 — stop it first |
| Search returns nothing | Confirm the index was seeded (check startup logs for "Permanently seeded N movies") |

---

## ⚠️ Security Note

- **Never commit real API keys.** This project reads them from env vars for this reason.
- If you've already pushed a hardcoded key to GitHub, **revoke/rotate it** immediately in the provider console.

---

## 📄 License

Private / educational project.
