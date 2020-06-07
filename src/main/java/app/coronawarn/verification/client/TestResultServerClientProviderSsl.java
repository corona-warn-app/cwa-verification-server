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

import feign.Client;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import lombok.RequiredArgsConstructor;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Component
@ConditionalOnProperty(value = "cwa-testresult-server.ssl.enabled", havingValue = "true")
@RequiredArgsConstructor
public class TestResultServerClientProviderSsl implements TestResultServerClientProvider {

  @Value("${cwa-testresult-server.ssl.key-store}")
  private String keyStorePath;
  @Value("${cwa-testresult-server.ssl.key-store-password}")
  private String keyStorePassword;
  @Value("${cwa-testresult-server.ssl.trust-store}")
  private String trustStorePath;
  @Value("${cwa-testresult-server.ssl.trust-store-password}")
  private String trustStorePassword;

  @Override
  public Client createFeignClient() {
    return new Client.Default(getSslSocketFactory(), new NoopHostnameVerifier());
  }

  private SSLSocketFactory getSslSocketFactory() {
    try {
      SSLContext sslContext = SSLContextBuilder
        .create()
        .loadKeyMaterial(ResourceUtils.getFile(keyStorePath),
          keyStorePassword.toCharArray(),
          keyStorePassword.toCharArray())
        .loadTrustMaterial(ResourceUtils.getFile(trustStorePath), trustStorePassword.toCharArray())
        .build();
      return sslContext.getSocketFactory();
    } catch (IOException | GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }
}
