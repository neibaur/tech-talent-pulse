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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = LocalFrontendCorsConfigOverrideTest.TestController.class)
@Import(LocalFrontendCorsConfig.class)
@ActiveProfiles("demo")
@TestPropertySource(properties = "tech-talent-pulse.web.cors.allowed-origins=http://localhost:5173")
class LocalFrontendCorsConfigOverrideTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void usesConfiguredAllowedOriginsWhenOverrideIsProvided() throws Exception {
    mockMvc
        .perform(
            options("/api/trends")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isOk())
        .andExpect(
            header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
  }

  @Test
  void doesNotAddDefaultOriginsWhenOverrideIsProvided() throws Exception {
    mockMvc
        .perform(
            options("/api/trends")
                .header(HttpHeaders.ORIGIN, "http://localhost:4321")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isForbidden());
  }

  @RestController
  static class TestController {

    @GetMapping("/api/trends")
    String trends() {
      return "ok";
    }
  }
}
