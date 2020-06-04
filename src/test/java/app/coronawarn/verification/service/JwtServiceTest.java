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

package app.coronawarn.verification.service;

import app.coronawarn.verification.VerificationApplication;
import app.coronawarn.verification.model.AuthorizationRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VerificationApplication.class)
public class JwtServiceTest {

  @Autowired
  private JwtService jwTService;

  /**
   * Test to validate an valid Token, with the {@link JwtService#validateToken(java.lang.String)} method.
   *
   * @throws java.io.UnsupportedEncodingException
   */
  @Test
  public void testValidToken() throws UnsupportedEncodingException {
    String jwToken = getJwtTestData(120, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    assertTrue(jwTService.validateToken(jwToken));
  }

  @Test
  /**
   * Test to validate an expired Token, with the {@link JwtService#validateToken(java.lang.String)} method.
   *
   * @throws java.io.UnsupportedEncodingException
   */

  public void testExpiredToken() throws UnsupportedEncodingException {
    String jwToken = getJwtTestData(0, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    assertFalse(jwTService.validateToken(jwToken));
  }

  private String getJwtTestData(final long expirationSecondsToAdd, AuthorizationRole... roles) throws UnsupportedEncodingException {
    final Map<String, List<String>> realm_accessMap = new HashMap<>();
    final List<String> roleNames = new ArrayList<>();
    for (AuthorizationRole role : roles) {
      roleNames.add(role.getRoleName());
    }

    String secret = "covid19";
    realm_accessMap.put("roles", roleNames);

    return Jwts.builder()
            .setExpiration(Date.from(Instant.now().plusSeconds(expirationSecondsToAdd)))
            .setIssuedAt(Date.from(Instant.now()))
            .setId("baeaa733-521e-4d2e-8abe-95bb440a9f5f")
            .setIssuer("http://localhost:8080/auth/realms/cwa")
            .setAudience("account")
            .setSubject("72b3b494-a0f4-49f5-b235-1e9f93c86e58")
            .claim("auth_time", "1590742669")
            .claim("iss", "http://localhost:8080/auth/realms/cwa")
            .claim("aud", "account")
            .claim("typ", "Bearer")
            .claim("azp", "verification-portal")
            .claim("session_state", "41cc4d83-e394-4d08-b887-28d8c5372d4a")
            .claim("acr", "0")
            .claim("realm_access", realm_accessMap)
            .claim("resource_access", new HashMap())
            .claim("scope", "openid profile email")
            .claim("email_verified", false)
            .claim("preferred_username", "test")
            .signWith(SignatureAlgorithm.HS256, secret.getBytes("UTF-8"))
            .compact();
  }
}
