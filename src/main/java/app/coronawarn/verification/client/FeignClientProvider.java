package app.coronawarn.verification.client;

import feign.Client;

public interface FeignClientProvider {
  Client createFeignClient();
}
