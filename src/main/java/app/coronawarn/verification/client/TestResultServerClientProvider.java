package app.coronawarn.verification.client;

import feign.Client;

public interface TestResultServerClientProvider {

  Client createFeignClient();

}
