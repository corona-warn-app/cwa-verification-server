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
import app.coronawarn.verification.client.IamClient;
import app.coronawarn.verification.config.VerificationApplicationConfig;
import app.coronawarn.verification.model.AuthorizationRole;
import app.coronawarn.verification.model.Certs;
import app.coronawarn.verification.model.Key;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Setter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JwtServiceTest
{
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String BEGIN_PEM = "-----BEGIN PUBLIC KEY-----";
  public static final String END_PEM = "-----END PUBLIC KEY-----";
  public static final String RSA = "RSA";
  
  private PublicKey publicKey;
  private PrivateKey privateKey;

  private JwtService jwtService = new JwtService(new IamClientMock(), new VerificationApplicationConfig());

  @Before
  public void setUp() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(RSA);
    keyGenerator.initialize(1024);
    KeyPair kp = keyGenerator.genKeyPair();
    publicKey = kp.getPublic();
    privateKey = kp.getPrivate();
  }

  /**
   * Test to validate an valid Token, with the
   * {@link JwtService#validateToken(java.lang.String, java.security.PublicKey)} method.
   *
   * @throws java.io.UnsupportedEncodingException
   * @throws java.security.NoSuchAlgorithmException
   */
  @Test
  public void testValidateToken() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String jwToken = getJwtTestData(3000, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    Assert.assertTrue(jwtService.validateToken(jwToken, publicKey));
  }
  
  /**
   * Test the negative case by not given public key, with the
   * {@link JwtService#validateToken(java.lang.String, java.security.PublicKey)} method.
   *
   * @throws java.io.UnsupportedEncodingException
   * @throws java.security.NoSuchAlgorithmException
   */
  @Test
  public void testValidateTokenByPublicKeyIsNull() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String jwToken = getJwtTestData(3000, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    Assert.assertFalse(jwtService.validateToken(jwToken, null));
  }  

  /**
   * Test is Token authorized, with the
   * {@link JwtService#isAuthorized(java.lang.String)} method.
   *
   * @throws java.io.UnsupportedEncodingException
   * @throws java.security.NoSuchAlgorithmException
   */
  @Test
  public void testAuthorizedToken() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String jwToken = getJwtTestData(3000, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    IamClientMock clientMock = createIamClientMock();
    jwtService = new JwtService(clientMock, new VerificationApplicationConfig());
    Assert.assertTrue(jwtService.isAuthorized(TOKEN_PREFIX + jwToken));
  }

  /**
   * Test to validate an expired Token, with the
   * {@link JwtService#validateToken(java.lang.String, java.security.PublicKey)} method.
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
    final Map<String, List<String>> realmAccessMap = new HashMap<>();
    final List<String> roleNames = new ArrayList<>();
    for (AuthorizationRole role : roles) {
      roleNames.add(role.getRoleName());
    }
    realmAccessMap.put("roles", roleNames);
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
            .claim("realm_access", realmAccessMap)
            .claim("resource_access", new HashMap<>())
            .claim("scope", "openid profile email")
            .claim("email_verified", false)
            .claim("preferred_username", "test")
            .signWith(SignatureAlgorithm.RS256, privateKey)
            .compact();
  }
  
  private IamClientMock createIamClientMock() {
    StringWriter writer = new StringWriter();
    PemWriter pemWriter = new PemWriter(writer);
    try {
      pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
      pemWriter.flush();
      pemWriter.close();
    } catch (IOException ex) {
      Logger.getLogger(JwtServiceTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    IamClientMock clientMock = new IamClientMock();
    String pem = writer.toString().replaceAll(System.lineSeparator(), "").replace(BEGIN_PEM, "").replace(END_PEM, "");
    clientMock.setPem(pem);
    return clientMock;
  }  

  public static class IamClientMock implements IamClient
  {
    @Setter
    String pem;

    @Override
    public Certs certs() {
      Certs certs = new Certs();
      List<Key> keys = new ArrayList<>();
      Key key = new Key();
      key.setKid("myqmD9sUqDTcCkprIixgYUh0dooxsCYL8HKSJ6fCMxc");
      key.setKty("RSA");
      key.setAlg("RS256");
      key.setUse("sig");
      key.setNn("v2PqGZrfX1TG19cKZWOTKWq3gBrHK4zT5dVEOS-a9vRk6Ab8XZiPIiX6K6d3w1srSpgol-UJ1gnNo9AoeCHOzwpOPBERfzcn4qKLkRE59dU_ZOfWUgWUN5awy_W5lYslBTCWj6_mEsLMgiAk2DWw9eqLsdmdNO_t-3HYs1Htn8do0Jb5cmuz0FOWSY-JrMxctG1EEbsjs9if3NdXL18s1yQK0UFkav2dfrOofdOu6fMInB0PzjjzJ7yCj-lwbZhnG1gHTfepRBvB-sV4U-uD-9lR3qUXX-VMDgLXO4-VlotWE0dwBhjrgzgkj92V2zCJx8V27UocnwBhQ0-377Zz3Q");
      key.setEe("AQAB");
      key.setX5t("s-pJCbOOR0JExZQ2Yh7-oeo_1tU");
      key.setX5tS256("9fxRTYYStVwlh8Cvoxcx9CxK3D9559HcYBOU19_981M");
      key.setX5c(Collections.singletonList(pem));
      keys.add(key);
      certs.setKeys(keys);
      return certs;
    }
  }
}
