/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, T-Systems International GmbH
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.verification.client;

import app.coronawarn.verification.exception.VerificationServerException;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.util.ResourceUtils;

@Configuration
@RequiredArgsConstructor

public class TestResultServerClientConfig {

  @Value("${cwa-testresult-server.ssl.enabled}")
  private boolean enabled;
  @Value("${cwa-testresult-server.ssl.one-way}")
  private boolean oneWay;
  @Value("${cwa-testresult-server.ssl.two-way}")
  private boolean twoWay;
  @Value("${cwa-testresult-server.ssl.hostname-verify}")
  private boolean hostnameVerify;
  @Value("${cwa-testresult-server.ssl.key-store}")
  private String keyStorePath;
  @Value("${cwa-testresult-server.ssl.key-store-password}")
  private char[] keyStorePassword;
  @Value("${cwa-testresult-server.ssl.trust-store}")
  private String trustStorePath;
  @Value("${cwa-testresult-server.ssl.trust-store-password}")
  private char[] trustStorePassword;

  /**
   * Configure the client dependent on the ssl properties.
   *
   * @return an Apache Http Client with or without SSL features
   */
  @Bean
  public Client client() {
    if (enabled) {
      return new ApacheHttpClient(
        HttpClientBuilder
          .create()
          .setSSLContext(getSslContext())
          .setSSLHostnameVerifier(getSslHostnameVerifier())
          .build()
      );
    }
    return new ApacheHttpClient(HttpClientBuilder.create()
      .setSSLHostnameVerifier(getSslHostnameVerifier())
      .build());
  }

  private SSLContext getSslContext() {
    try {
      SSLContextBuilder builder = SSLContextBuilder
        .create();
      if (oneWay) {
        builder.loadTrustMaterial(ResourceUtils.getFile(trustStorePath),
          trustStorePassword);
      }
      if (twoWay) {
        builder.loadKeyMaterial(ResourceUtils.getFile(keyStorePath),
          keyStorePassword,
          keyStorePassword);
      }
      return builder.build();
    } catch (IOException | GeneralSecurityException e) {
      throw new VerificationServerException(HttpStatus.INTERNAL_SERVER_ERROR, "The SSL context could not be loaded.");
    }
  }

  private HostnameVerifier getSslHostnameVerifier() {
    return hostnameVerify ? new DefaultHostnameVerifier() : new NoopHostnameVerifier();
  }

}
