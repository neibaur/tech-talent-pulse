package com.techtalentpulse.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;

class DemoDatasourcePropertiesTest {

  @Test
  void demoDatasourceDefaultsResolveToConcreteLocalValues() throws IOException {
    MockEnvironment environment = new MockEnvironment();
    for (PropertySource<?> propertySource :
        new YamlPropertySourceLoader()
            .load("demo", new ClassPathResource("application-demo.yml"))) {
      environment.getPropertySources().addLast(propertySource);
    }

    String url =
        environment.resolveRequiredPlaceholders(environment.getProperty("spring.datasource.url"));
    String username =
        environment.resolveRequiredPlaceholders(
            environment.getProperty("spring.datasource.username"));
    String password =
        environment.resolveRequiredPlaceholders(
            environment.getProperty("spring.datasource.password"));

    assertThat(url).isEqualTo("jdbc:postgresql://localhost:5432/tech_talent_pulse");
    assertThat(username).isEqualTo("tech_talent_pulse");
    assertThat(password).isEqualTo("tech_talent_pulse");
    assertThat(url).doesNotContain("${");
    assertThat(username).doesNotContain("${");
    assertThat(password).doesNotContain("${");
  }
}
