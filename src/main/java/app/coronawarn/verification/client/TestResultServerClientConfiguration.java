package app.coronawarn.verification.client;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestResultServerClientConfiguration {

  private final FeignClientProvider feignClientProvider;

  public TestResultServerClientConfiguration(FeignClientProvider feignClientProvider) {
    this.feignClientProvider = feignClientProvider;
  }

  @Bean
  public Client feignClient() {
    return feignClientProvider.createFeignClient();
  }

}
