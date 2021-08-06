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

import app.coronawarn.verification.client.IamClient;
import app.coronawarn.verification.config.VerificationApplicationConfig;
import app.coronawarn.verification.model.AuthorizationRole;
import app.coronawarn.verification.model.Certs;
import app.coronawarn.verification.model.Key;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class JwtServiceTest {
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String BEGIN_PEM = "-----BEGIN PUBLIC KEY-----";
  public static final String END_PEM = "-----END PUBLIC KEY-----";
  public static final String RSA = "RSA";

  private PublicKey publicKey;
  private PrivateKey privateKey;
  private String cert;

  static {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
  }

  private final VerificationApplicationConfig config = new VerificationApplicationConfig();

  private JwtService jwtService = new JwtService(new IamClientMock(), config);

  @BeforeEach
  public void setUp() throws NoSuchAlgorithmException {
    VerificationApplicationConfig.Jwt jwtConfig = new VerificationApplicationConfig.Jwt();
    jwtConfig.setEnabled(true);
    config.setJwt(jwtConfig);

    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(RSA);
    keyGenerator.initialize(2048);
    KeyPair kp = keyGenerator.genKeyPair();
    publicKey = kp.getPublic();
    privateKey = kp.getPrivate();
    try {
      cert = generateTestCertificate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Test to validate an valid Token, with the
   * {@link JwtService#validateToken(String, PublicKey, List)} method.
   *
   * @throws java.io.UnsupportedEncodingException if the test cannot be performed.
   * @throws java.security.NoSuchAlgorithmException if the test cannot be performed.
   */
  @Test
  public void validateToken() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String jwToken = getJwtTestData(3000, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    Assertions.assertTrue(jwtService.validateToken(jwToken, publicKey, Collections.emptyList()));

    jwToken = getJwtTestData(3000, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    Assertions.assertFalse(jwtService.validateToken(jwToken, publicKey, List.of(AuthorizationRole.AUTH_C19_HOTLINE_EVENT)));
  }

  /**
   * Test the negative case by not given public key, with the
   * {@link JwtService#validateToken(String, PublicKey, List)} method.
   *
   * @throws java.io.UnsupportedEncodingException if the test cannot be performed.
   * @throws java.security.NoSuchAlgorithmException if the test cannot be performed.
   */
  @Test
  public void validateTokenByPublicKeyIsNull() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String jwToken = getJwtTestData(3000, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    Assertions.assertFalse(jwtService.validateToken(jwToken, null, Collections.emptyList()));
  }  

  /**
   * Test is Token authorized, with the {@link JwtService#isAuthorized(String, List)} method.
   *
   * @throws java.io.UnsupportedEncodingException if the test cannot be performed.
   * @throws java.security.NoSuchAlgorithmException if the test cannot be performed.
   */
  @Test
  public void tokenIsAuthorized() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String jwToken = getJwtTestData(3000, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    IamClientMock clientMock = createIamClientMock();
    jwtService = new JwtService(clientMock, config);
    Assertions.assertTrue(jwtService.isAuthorized(TOKEN_PREFIX + jwToken, Collections.emptyList()));

    Assertions.assertFalse(jwtService.isAuthorized(TOKEN_PREFIX + getJwtTestData(3000, AuthorizationRole.AUTH_C19_HOTLINE), List.of(AuthorizationRole.AUTH_C19_HOTLINE_EVENT)));
  }

  /**
   * Test to validate an expired Token, with the
   * {@link JwtService#validateToken(String, PublicKey, List)} method.
   *
   * @throws java.io.UnsupportedEncodingException if the test cannot be performed.
   * @throws java.security.NoSuchAlgorithmException if the test cannot be performed.
   */
  @Test
  public void validateExpiredToken() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String jwToken = getJwtTestData(0, AuthorizationRole.AUTH_C19_HOTLINE, AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    Assertions.assertFalse(jwtService.validateToken(jwToken, publicKey, Collections.emptyList()));
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
      .signWith(privateKey, SignatureAlgorithm.RS256)
      .compact();
  }

  private String generateTestCertificate() throws Exception {
//    KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", "BC");
//    KeyPair pair = kpGen.generateKeyPair();
    LocalDateTime startDate = LocalDate.now().atStartOfDay();
    X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
      new X500Name("CN=ca"),
      new BigInteger("0"),
      Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant()),
      Date.from(startDate.plusDays(3650).atZone(ZoneId.systemDefault()).toInstant()),
      new X500Name("CN=ca"),
      SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA512WithRSAEncryption");
    ContentSigner signer = csBuilder.build(privateKey);
    X509CertificateHolder holder = builder.build(signer);
    StringWriter writer = new StringWriter();
    PemWriter pemWriter = new PemWriter(writer);
    try {
      pemWriter.writeObject(new PemObject("CERTIFICATE", holder.toASN1Structure().getEncoded()));
      pemWriter.flush();
      pemWriter.close();
    } catch (IOException ex) {
      log.warn("Error writeObject: {}.", ex.getMessage());
    }
    return writer.toString();
  }

  private IamClientMock createIamClientMock() {
    IamClientMock clientMock = new IamClientMock();
    clientMock.setPem(cert.replaceAll(System.lineSeparator(), "").replace(JwtService.BEGIN_CERT, "").replace(JwtService.END_CERT, ""));
    return clientMock;
  }

  public static class IamClientMock implements IamClient {
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
