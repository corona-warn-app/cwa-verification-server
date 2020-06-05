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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
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

  PublicKey publicKey;
  PrivateKey privateKey;
  
  @Autowired
  private JwtService jwtService;  
  
  @Before
  public void setUp() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
    keyGenerator.initialize(1024);
    KeyPair kp = keyGenerator.genKeyPair();
    publicKey = kp.getPublic();
    privateKey = kp.getPrivate();    
  }  
  

  /**
   * Test to validate an valid Token, with the {@link JwtService#validateToken(java.lang.String)} method.
   *
   * @throws java.io.UnsupportedEncodingException
   * @throws java.security.NoSuchAlgorithmException
   */
  @Test
  public void testValidateToken() throws UnsupportedEncodingException, NoSuchAlgorithmException  {
    String jwToken = getJwtTestData(3000, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    Assert.assertTrue(jwtService.validateToken(jwToken, publicKey));
  }
  
  /**
   * Test to validate an valid Token, with the {@link JwtService#validateToken(java.lang.String)} method.
   *
   * @throws java.io.UnsupportedEncodingException
   * @throws java.security.NoSuchAlgorithmException
   */
  // @Test
  public void testAuthorizedToken() throws UnsupportedEncodingException, NoSuchAlgorithmException  {
    String jwToken = getJwtTestData(3000, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    Assert.assertTrue(jwtService.isAuthorized("Bearer " + jwToken));
  }  
  
  /**
   * Test to validate an expired Token, with the {@link JwtService#validateToken(java.lang.String)} method.
   *
   * @throws java.io.UnsupportedEncodingException
   * @throws java.security.NoSuchAlgorithmException
   */
  @Test
  public void testExpiredToken() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String jwToken = getJwtTestData(0, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    Assert.assertFalse(jwtService.validateToken(jwToken, publicKey));
  }

  private String getJwtTestData(final long expirationSecondsToAdd, AuthorizationRole... roles) throws UnsupportedEncodingException, NoSuchAlgorithmException {
    final Map<String, List<String>> realm_accessMap = new HashMap<>();
    final List<String> roleNames = new ArrayList<>();
    for (AuthorizationRole role : roles) {
      roleNames.add(role.getRoleName());
    }
    
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
      .signWith(SignatureAlgorithm.RS256, privateKey)
      .compact();
  }
}
