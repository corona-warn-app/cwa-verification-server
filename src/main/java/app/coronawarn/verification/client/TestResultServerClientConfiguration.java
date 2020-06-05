package app.coronawarn.verification.client;

import feign.Client;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;

@Configuration
public class TestResultServerClientConfiguration {

  @Autowired
  Environment environment;

  /**
   * Creates a feign client.
   * @return default feign client with mTLS configuration if enabled.
   */
  @Bean
  public Client feignClient() {
    boolean isMutualAuthEnabled = Boolean.parseBoolean(
      environment.getProperty("cwa-testresult-server.ssl.two-way-authentication-enabled"));
    if (isMutualAuthEnabled) {
      return new Client.Default(getSslSocketFactory(), new NoopHostnameVerifier());
    } else {
      return new Client.Default(null, new NoopHostnameVerifier());
    }

  }

  private SSLSocketFactory getSslSocketFactory() {
    try {
      String keyStorePath = environment.getProperty("cwa-testresult-server.ssl.key-store");
      String keyStorePassword = environment
        .getProperty("cwa-testresult-server.ssl.key-store-password");
      String keyPassword = environment.getProperty("cwa-testresult-server.ssl.key-password");

      String trustStorePath = environment.getProperty("cwa-testresult-server.ssl.trust-store");
      String trustStorePassword = environment
        .getProperty("cwa-testresult-server.ssl.trust-store-password");

      SSLContext sslContext = SSLContextBuilder
        .create()
        .loadKeyMaterial(ResourceUtils.getFile(keyStorePath), keyStorePassword.toCharArray(),
          keyPassword.toCharArray())
        .loadTrustMaterial(ResourceUtils.getFile(trustStorePath), trustStorePassword.toCharArray())
        .build();
      return sslContext.getSocketFactory();
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }
}
