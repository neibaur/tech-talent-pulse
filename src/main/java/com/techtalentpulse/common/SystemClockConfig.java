package com.techtalentpulse.common;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SystemClockConfig {

  @Bean
  Clock systemClock() {
    return Clock.systemUTC();
  }
}
