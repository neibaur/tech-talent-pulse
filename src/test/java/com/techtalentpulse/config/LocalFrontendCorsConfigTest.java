package com.techtalentpulse.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = LocalFrontendCorsConfigTest.TestController.class)
@Import(LocalFrontendCorsConfig.class)
@ActiveProfiles("demo")
class LocalFrontendCorsConfigTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void allowsCommonAstroDevServerOriginsForApiRequests() throws Exception {
    assertAllowedOrigin("http://localhost:4321");
    assertAllowedOrigin("http://127.0.0.1:4321");
    assertAllowedOrigin("http://localhost:4322");
    assertAllowedOrigin("http://127.0.0.1:4322");
  }

  @Test
  void allowsCommonAstroDevServerOriginsForHealthRequests() throws Exception {
    mockMvc
        .perform(
            options("/actuator/health")
                .header(HttpHeaders.ORIGIN, "http://127.0.0.1:4321")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isOk())
        .andExpect(
            header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://127.0.0.1:4321"));
  }

  @Test
  void doesNotAllowUnexpectedOrigins() throws Exception {
    mockMvc
        .perform(
            options("/api/trends")
                .header(HttpHeaders.ORIGIN, "http://example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isForbidden());
  }

  private void assertAllowedOrigin(String origin) throws Exception {
    mockMvc
        .perform(
            options("/api/trends")
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
  }

  @RestController
  static class TestController {

    @GetMapping("/api/trends")
    String trends() {
      return "ok";
    }
  }
}
