package com.techtalentpulse;

import com.techtalentpulse.config.StackExchangeApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(StackExchangeApiProperties.class)
@EnableScheduling
public class TechTalentPulseApplication {

  public static void main(String[] args) {
    SpringApplication.run(TechTalentPulseApplication.class, args);
  }
}
