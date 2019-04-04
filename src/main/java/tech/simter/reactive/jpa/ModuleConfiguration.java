package tech.simter.reactive.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * All configuration for this module.
 *
 * @author RJ
 */
@Configuration("tech.simter.reactive.jpa.ModuleConfiguration")
@ComponentScan("tech.simter.reactive.jpa")
public class ModuleConfiguration {
  /**
   * Register a default implementation of {@link ReactiveJpaWrapper}.
   */
  @Bean
  public ReactiveJpaWrapper reactiveJpaWrapper() {
    return new ReactiveJpaWrapper() {
    };
  }
}