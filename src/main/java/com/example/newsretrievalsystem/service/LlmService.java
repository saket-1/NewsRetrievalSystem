package com.example.newsretrievalsystem.service;

import com.example.newsretrievalsystem.dto.LlmResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LlmService {

   // @Value("${google.gemini.api.key}")
  //  private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    String projectId = "gen-lang-client-0174311618"; // Get this from your Google Cloud Console
    String location = "us-central1";
    String modelName = "gemini-2.5-flash";

    public LlmResponseDto analyzeQuery(String userQuery) throws IOException {
        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);

            String prompt = buildPrompt(userQuery);
            GenerateContentResponse response = model.generateContent(prompt);
            String jsonResponse = ResponseHandler.getText(response);

            // Clean the response to ensure it's valid JSON
            String cleanedJson = jsonResponse.replace("```json", "").replace("```", "").trim();

            return objectMapper.readValue(cleanedJson, LlmResponseDto.class);
        }
    }

    private String buildPrompt(String userQuery) {
        return "Analyze the user's news query to extract the intent and relevant entities. " +
                "The possible intents are: 'category', 'source', 'search', 'nearby', or 'general'. " +
                "Return the response ONLY in a valid JSON format with the keys 'intent' and 'entities'. " +
                "The 'entities' object should contain one or more of the following keys: 'category', 'source_name', 'search_query', 'location'.\n\n" +
                "Examples:\n" +
                "Query: 'Top technology news from the New York Times'\n" +
                "Response: {\"intent\": \"source\", \"entities\": {\"source_name\": \"New York Times\", \"category\": \"Technology\"}}\n\n" +
                "Query: 'Latest developments in the Elon Musk Twitter acquisition near Palo Alto'\n" +
                "Response: {\"intent\": \"nearby\", \"entities\": {\"search_query\": \"Elon Musk Twitter acquisition\", \"location\": \"Palo Alto\"}}\n\n" +
                "Query: 'Find articles about artificial intelligence'\n" +
                "Response: {\"intent\": \"search\", \"entities\": {\"search_query\": \"artificial intelligence\"}}\n\n" +
                "Query: 'What is happening in sports?'\n" +
                "Response: {\"intent\": \"category\", \"entities\": {\"category\": \"Sports\"}}\n\n" +
                "User Query to analyze:\n" +
                "Query: '" + userQuery + "'\n" +
                "Response:";
    }

    public String summarizeArticle(String title, String description) throws IOException {

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);

            String prompt = "Summarize the following news article content into a single, concise sentence. " +
                    "Title: \"" + title + "\". " +
                    "Description: \"" + description + "\".";

            GenerateContentResponse response = model.generateContent(prompt);
            return ResponseHandler.getText(response).trim();
        } catch (Exception e) {
            // In a real application, you'd have more robust error handling.
            // For now, we return a default message on failure.
            System.err.println("Error summarizing article: " + e.getMessage());
            return "Summary could not be generated.";
        }
    }
}