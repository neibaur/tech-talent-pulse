package com.techtalentpulse.ingestion.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtalentpulse.config.StackExchangeApiProperties;
import com.techtalentpulse.ingestion.application.StackOverflowQuestionPayload;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class StackOverflowRestClientTest {

  @Test
  void fetchRecentQuestionsReturnsRawQuestionPayloads() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    StackExchangeApiProperties properties =
        new StackExchangeApiProperties(
            "https://api.stackexchange.com/2.3", "stackoverflow", 2, List.of("java"), false);
    StackOverflowRestClient client =
        new StackOverflowRestClient(builder, properties, new ObjectMapper());

    server
        .expect(requestTo(startsWith("https://api.stackexchange.com/2.3/questions")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(queryParam("site", "stackoverflow"))
        .andExpect(queryParam("tagged", "java"))
        .andExpect(queryParam("pagesize", "2"))
        .andExpect(queryParam("order", "desc"))
        .andExpect(queryParam("sort", "creation"))
        .andRespond(
            withSuccess(
                """
                {
                  "items": [
                    {
                      "question_id": 1001,
                      "title": "How do I configure Java records?",
                      "tags": ["java"]
                    }
                  ],
                  "has_more": false
                }
                """,
                MediaType.APPLICATION_JSON));

    List<StackOverflowQuestionPayload> questions = client.fetchRecentQuestions("java", 2);

    assertThat(questions).hasSize(1);
    assertThat(questions.getFirst().providerId()).isEqualTo("1001");
    assertThat(questions.getFirst().payload()).contains("\"question_id\":1001");
    server.verify();
  }
}
