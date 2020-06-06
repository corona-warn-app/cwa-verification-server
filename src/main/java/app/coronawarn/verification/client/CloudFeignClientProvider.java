package app.coronawarn.verification.client;


import feign.Client;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Component
@Profile("ssl-client-testresult")
public class CloudFeignClientProvider implements FeignClientProvider {

  Environment environment;

  public CloudFeignClientProvider(Environment environment) {
    this.environment = environment;
  }

  @Override
  public Client createFeignClient() {
    return new Client.Default(getSslSocketFactory(), new NoopHostnameVerifier());
  }

  private SSLSocketFactory getSslSocketFactory() {
    try {
      String keyStorePath = environment.getProperty("client.ssl.key-store");
      String keyStorePassword = environment.getProperty("client.ssl.key-store-password");
      String keyPassword = environment.getProperty("client.ssl.key-password");

      String trustStorePath = environment.getProperty("client.ssl.testresult.trust-store");
      String trustStorePassword = environment.getProperty("client.ssl.testresult.trust-store-password");

      SSLContext sslContext = SSLContextBuilder
        .create()
        .loadKeyMaterial(ResourceUtils.getFile(keyStorePath), keyStorePassword.toCharArray(),
          keyPassword.toCharArray())
        .loadTrustMaterial(ResourceUtils.getFile(trustStorePath), trustStorePassword.toCharArray())
        .build();
      return sslContext.getSocketFactory();
    } catch (IOException | GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }
}
