package com.netflix.recommender.service;

import com.netflix.recommender.model.MovieData;
import com.netflix.recommender.model.MovieMatch;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovieService {

    private final JsonMapper jsonMapper;
    private final VectorStore vectorStore;

    private final Map<String, String> titleToDescription = new HashMap<>();

    /** Becomes true only if Pinecone is reachable and seeded/skipped successfully. */
    private boolean vectorStoreAvailable = false;

    public MovieService(JsonMapper jsonMapper,
                        VectorStore vectorStore) {
        this.jsonMapper = jsonMapper;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void initializeMovies() {
        // Always load metadata (title -> description) from movies.json.
        // This is cheap and never calls the embedding API.
        try {
            loadMetadata();
        } catch (IOException e) {
            System.err.println("Could not load movies.json: " + e.getMessage());
            return;
        }

        // Try to talk to Pinecone. If it fails (bad/missing key, no network),
        // log a warning but DO NOT crash startup.
        try {
            if (isSeeded()) {
                System.out.println("Pinecone already seeded (" + titleToDescription.size()
                        + " movies). Skipping embedding.");
            } else {
                seedMovies();
            }
            vectorStoreAvailable = true;
        } catch (RuntimeException e) {
            System.err.println("WARNING: Pinecone unavailable (" + e.getMessage()
                    + "). The app will start, but search/similar will be disabled.");
            vectorStoreAvailable = false;
        }
    }

    private void loadMetadata() throws IOException {
        ClassPathResource resource = new ClassPathResource("movies.json");
        try (InputStream inputStream = resource.getInputStream()) {
            List<MovieData> movies =
                    jsonMapper.readValue(inputStream, new TypeReference<>() {});
            for (MovieData movieData : movies) {
                titleToDescription.put(movieData.getTitle(), movieData.getDescription());
            }
        }
    }

    private boolean isSeeded() {
        List<Document> existing = vectorStore.similaritySearch(
                SearchRequest.builder().query("movie").topK(1).build());
        return !existing.isEmpty();
    }

    private void seedMovies() {
        List<Document> documents = new ArrayList<>();

        for (Map.Entry<String, String> entry : titleToDescription.entrySet()) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", entry.getKey());
            metadata.put("description", entry.getValue());

            documents.add(new Document(entry.getValue(), metadata));
        }

        vectorStore.add(documents);
        System.out.println("Permanently seeded " + documents.size()
                + " movies into Pinecone.");
    }

    public List<MovieMatch> search(String query) {
        if (!vectorStoreAvailable) {
            return unavailableResponse();
        }

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(4).build());

        List<MovieMatch> matches = new ArrayList<>();
        for (Document doc : results) {
            String title = (String) doc.getMetadata().get("title");
            String description = (String) doc.getMetadata().get("description");
            double similarity = doc.getScore() != null ? doc.getScore() : 0.0;

            matches.add(new MovieMatch(title, description, similarity));
        }

        return matches;
    }

    public List<MovieMatch> similarMovies(String title) {
        if (!vectorStoreAvailable) {
            return unavailableResponse();
        }

        String description = titleToDescription.get(title);
        if (description == null) {
            throw new IllegalArgumentException("Movie not found: " + title);
        }

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(description).topK(4).build());

        List<MovieMatch> matches = new ArrayList<>();
        for (Document doc : results) {
            String resultTitle = (String) doc.getMetadata().get("title");
            if (resultTitle != null && resultTitle.equalsIgnoreCase(title)) {
                continue; // skip the selected movie itself
            }

            String resultDescription = (String) doc.getMetadata().get("description");
            double similarity = doc.getScore() != null ? doc.getScore() : 0.0;

            matches.add(new MovieMatch(resultTitle, resultDescription, similarity));
        }

        return matches;
    }

    private List<MovieMatch> unavailableResponse() {
        List<MovieMatch> list = new ArrayList<>();
        list.add(new MovieMatch("Pinecone not configured", "Set PINECONE_API_KEY and restart.", 0.0));
        return list;
    }
}