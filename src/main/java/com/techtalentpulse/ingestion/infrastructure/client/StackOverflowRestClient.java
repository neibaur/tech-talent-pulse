package com.techtalentpulse.ingestion.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtalentpulse.config.StackExchangeApiProperties;
import com.techtalentpulse.ingestion.application.StackOverflowQuestionClient;
import com.techtalentpulse.ingestion.application.StackOverflowQuestionPayload;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StackOverflowRestClient implements StackOverflowQuestionClient {

  private final RestClient restClient;
  private final StackExchangeApiProperties properties;
  private final ObjectMapper objectMapper;

  public StackOverflowRestClient(
      RestClient.Builder restClientBuilder,
      StackExchangeApiProperties properties,
      ObjectMapper objectMapper) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    this.properties = properties;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<StackOverflowQuestionPayload> fetchRecentQuestions(String tag, int pageSize) {
    String responseBody =
        restClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/questions")
                        .queryParam("site", properties.site())
                        .queryParam("tagged", tag)
                        .queryParam("pagesize", pageSize)
                        .queryParam("order", "desc")
                        .queryParam("sort", "creation")
                        .build())
            .retrieve()
            .body(String.class);

    JsonNode response = parseResponse(responseBody);
    JsonNode items = response == null ? objectMapper.createArrayNode() : response.path("items");
    List<StackOverflowQuestionPayload> questions = new ArrayList<>();

    if (!items.isArray()) {
      return questions;
    }

    for (JsonNode item : items) {
      JsonNode questionId = item.path("question_id");
      if (questionId.isMissingNode() || questionId.isNull()) {
        continue;
      }
      questions.add(new StackOverflowQuestionPayload(questionId.asText(), writePayload(item)));
    }

    return questions;
  }

  private JsonNode parseResponse(String responseBody) {
    if (responseBody == null || responseBody.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readTree(responseBody);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException(
          "Unable to parse Stack Overflow question response", exception);
    }
  }

  private String writePayload(JsonNode item) {
    try {
      return objectMapper.writeValueAsString(item);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException(
          "Unable to serialize Stack Overflow question payload", exception);
    }
  }
}
