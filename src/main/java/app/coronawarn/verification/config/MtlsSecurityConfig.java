/*-
 * ---license-start
 * Corona-Warn-App / cwa-verification
 * ---
 * Copyright (C) 2020 - 2022 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.verification.config;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.x509.X509PrincipalExtractor;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.server.ResponseStatusException;

@Configuration
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "server.ssl.client-auth", havingValue = "need")
public class MtlsSecurityConfig extends WebSecurityConfigurerAdapter {

  private final VerificationApplicationConfig config;

  @Bean
  protected HttpFirewall strictFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowedHttpMethods(Arrays.asList(
      HttpMethod.GET.name(),
      HttpMethod.POST.name()
    ));
    return firewall;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .authorizeRequests()
      .mvcMatchers("/api/**").authenticated().and()
      .requiresChannel().mvcMatchers("/api/**").requiresSecure().and()
      .x509().x509PrincipalExtractor(new ThumbprintX509PrincipalExtractor()).userDetailsService(userDetailsService())
      .and().authorizeRequests()
      .mvcMatchers("/version/**").permitAll()
      .mvcMatchers("/actuator/**").permitAll()
      .anyRequest().denyAll()
      .and().csrf().disable();
  }

  @Override
  public UserDetailsService userDetailsService() {
    return hash -> {

      boolean allowed = Stream.of(config.getAllowedClientCertificates()
          .split(","))
        .map(String::trim)
        .anyMatch(entry -> entry.equalsIgnoreCase(hash));

      if (allowed) {
        return new User(hash, "", Collections.emptyList());
      } else {
        log.error("Failed to authenticate cert with hash {}", hash);
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
      }
    };
  }

  private static class ThumbprintX509PrincipalExtractor implements X509PrincipalExtractor {

    @Override
    public Object extractPrincipal(X509Certificate x509Certificate) {

      try {
        String hash = DigestUtils.sha256Hex(x509Certificate.getEncoded());
        log.debug("Accessed by Subject {} Hash {}", x509Certificate.getSubjectDN().getName(), hash);
        return hash;
      } catch (CertificateEncodingException e) {
        log.error("Failed to extract bytes from certificate");
        return null;
      }
    }
  }
}

