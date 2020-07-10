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

package app.coronawarn.verification;

import app.coronawarn.verification.config.VerificationApplicationConfig;
import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.AuthorizationRole;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.RegistrationTokenKeyType;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.model.Tan;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.model.TanType;
import app.coronawarn.verification.model.TestResult;
import app.coronawarn.verification.repository.VerificationAppSessionRepository;
import app.coronawarn.verification.service.JwtService;
import app.coronawarn.verification.service.TanService;
import app.coronawarn.verification.service.TestResultServerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This is the test class for the verification application.
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VerificationApplication.class)
@AutoConfigureMockMvc
public class VerificationApplicationTest {

  public static final String TEST_GUI_HASH = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
  public static final String TEST_INVALID_GUI_HASH = "f0e4c2f76c58916ec2b";
  public static final String TEST_TELE_TAN = "R3ZNUeV";
  public static final String TEST_TELE_TAN_HASH = "eeaa54dc40aa84f587e3bc0cbbf18f7c05891558a5fe1348d52f3277794d8730";
  public static final String TEST_INVALID_REG_TOK = "1234567890";
  public static final String TEST_REG_TOK = "1ea6ce8a-9740-41ea-bb37-0242ac130002";
  public static final String TEST_REG_TOK_HASH = "0199effab87800689c15c08e234db54f088cc365132ffc230e882b82cd3ecf95";
  public static final TestResult TEST_LAB_POSITIVE_RESULT = new TestResult(2);
  public static final TestResult TEST_LAB_NEGATIVE_RESULT = new TestResult(1);
  public static final String TEST_TAN = "1819d933-45f6-4e3c-80c7-eeffd2d44ee6";
  public static final String TEST_INVALID_TAN = "1ea6ce8a-9740-11ea-is-invalid";
  public static final TanSourceOfTrust TEST_SOT = TanSourceOfTrust.CONNECTED_LAB;
  public static final String TEST_HASHED_TAN = "cfb5368fc0fca485847acb28e6a96c958bb6ab7350ac766be88ad13841750231";
  public static final String TEST_TAN_TYPE = "TAN";
  private static final LocalDateTime TAN_VALID_UNTIL_IN_DAYS = LocalDateTime.now().plusDays(7);
  private static final String PREFIX_API_VERSION = "/version/v1";
  private static final String REGISTRATION_TOKEN_URI = "/registrationToken";

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private TestResultServerService testResultServerService;
  @MockBean
  private TanService tanService;
  @Autowired
  private VerificationAppSessionRepository appSessionrepository;
  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private VerificationApplicationConfig verificationApplicationConfig;

  @MockBean
  private JwtService jwtService;

  private static final String TAN_VERIFICATION_URI = "/tan/verify";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test generateTAN.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTan() throws Exception {
    log.info("process callGenerateTan()");

    prepareAppSessionTestData();
    doReturn(TEST_LAB_POSITIVE_RESULT).when(testResultServerService).result(any());

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
      .andExpect(status().isCreated());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals("Verification Failed: Amount of verification entries is not 1 (Result=" + count + "). ", 1, count);

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertEquals(TEST_GUI_HASH, verificationList.get(0).getHashedGuid());
    assertEquals(AppSessionSourceOfTrust.HASHED_GUID, verificationList.get(0).getSourceOfTrust());
    assertEquals(TEST_REG_TOK_HASH, verificationList.get(0).getRegistrationTokenHash());

  }

  /**
   * Test generateTAN with an unknown registration token.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTanByUnknownToken() throws Exception {
    log.info("process callGenerateTanByUnknownToken()");

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test generateTAN with an invalid registration token.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTanByInvalidToken() throws Exception {
    log.info("process callGenerateTanByInvalidToken()");

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new RegistrationToken(TEST_INVALID_REG_TOK))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test generateTAN with an negative test result from the lab-server.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTanWithNegativeCovidResult() throws Exception {
    log.info("process callGenerateTanWithNegativeCovidResult()");
    prepareAppSessionTestData();
    doReturn(TEST_LAB_NEGATIVE_RESULT).when(testResultServerService).result(any());

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test generateTAN with an registration token where the tancounter maximum is reached.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTanWithTanCounterMaximum() throws Exception {
    log.info("process callGenerateTanWithTanCounterMaximum()");
    appSessionrepository.deleteAll();
    VerificationAppSession appSessionTestData = getAppSessionTestData();
    int tancountermax = verificationApplicationConfig.getAppsession().getTancountermax();
    appSessionTestData.setTanCounter(tancountermax);
    appSessionrepository.save(appSessionTestData);

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test generateTAN with an registration token connected to an appsession based on a tele Tan.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTanWithTeleTanAppSession() throws Exception {
    log.info("process callGenerateTanWithTeleTanAppSession()");
    appSessionrepository.deleteAll();
    VerificationAppSession appSessionTestData = getAppSessionTestData();
    appSessionTestData.setSourceOfTrust(AppSessionSourceOfTrust.TELETAN);
    appSessionrepository.save(appSessionTestData);
    doReturn(TEST_LAB_NEGATIVE_RESULT).when(testResultServerService).result(any());

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
      .andExpect(status().isCreated());
  }

  /**
   * Test generateTAN with an unknown source of trust in the appsession.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTanWithUnknownSourceOfTrust() throws Exception {
    log.info("process callGenerateTanWithUnknownSourceOfTrust()");
    appSessionrepository.deleteAll();
    VerificationAppSession appSessionTestData = getAppSessionTestData();
    appSessionTestData.setSourceOfTrust(AppSessionSourceOfTrust.HASHED_GUID);
    appSessionrepository.save(appSessionTestData);
    doReturn(TEST_LAB_NEGATIVE_RESULT).when(testResultServerService).result(any());

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test the generation of a tele Tan.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTeleTAN() throws Exception {
    log.info("process callGenerateTeleTAN()");

    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
    keyGenerator.initialize(2048);
    KeyPair kp = keyGenerator.genKeyPair();
    String jwtString = getJwtTestData(3000, kp.getPrivate(), AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);

    given(this.jwtService.isAuthorized(any())).willReturn(Boolean.TRUE);
    given(this.jwtService.getPublicKey()).willReturn(kp.getPublic());
    when(this.jwtService.validateToken(jwtString, kp.getPublic())).thenCallRealMethod();

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan/teletan").header(JwtService.HEADER_NAME_AUTHORIZATION, JwtService.TOKEN_PREFIX + jwtString))
      .andExpect(status().isCreated());
  }

  /**
   * Test the generation of a tele Tan, when the jwt is not authorized.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTeleTanUnauthorized() throws Exception {
    log.info("process callGenerateTeleTanUnauthorized()");

    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
    keyGenerator.initialize(2048);
    KeyPair kp = keyGenerator.genKeyPair();
    given(this.jwtService.isAuthorized(any())).willReturn(false);
    given(this.jwtService.getPublicKey()).willReturn(kp.getPublic());
    String jwtString = getJwtTestData(3000, kp.getPrivate(), AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    mockMvc.perform(post(PREFIX_API_VERSION + "/tan/teletan").header(JwtService.HEADER_NAME_AUTHORIZATION, JwtService.TOKEN_PREFIX + jwtString))
      .andExpect(status().isUnauthorized());
  }

  /**
   * Test get registration token by a guid.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByGuid() throws Exception {
    log.info("process callGetRegistrationTokenByGuid() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TEST_GUI_HASH, RegistrationTokenKeyType.GUID);
    mockMvc.perform(post(PREFIX_API_VERSION + REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.registrationToken").exists());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals("Verification Failed: Amount of verification entries is not 1 (Result=" + count + "). ", 1, count);

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertEquals(TEST_GUI_HASH, verificationList.get(0).getHashedGuid());
    assertEquals(AppSessionSourceOfTrust.HASHED_GUID, verificationList.get(0).getSourceOfTrust());
    assertNotNull(verificationList.get(0).getRegistrationTokenHash());
  }

  /**
   * Test get registration token by a keytype which is null.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenWithNullKeyType() throws Exception {
    log.info("process callGetRegistrationTokenWithNullKeyType() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TEST_GUI_HASH, null);
    mockMvc.perform(post(PREFIX_API_VERSION + REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(request)))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test get registration token by a key which is null.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenWithNullKey() throws Exception {
    log.info("process callGetRegistrationTokenWithNullKey() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(null, RegistrationTokenKeyType.GUID);
    mockMvc.perform(post(PREFIX_API_VERSION + REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(request)))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test get registration token by a tele tan.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByTeleTan() throws Exception {
    log.info("process callGetRegistrationTokenByTeleTan() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TEST_TELE_TAN, RegistrationTokenKeyType.TELETAN);
    given(this.tanService.verifyTeleTan(TEST_TELE_TAN)).willReturn(true);
    given(this.tanService.isTeleTanValid(TEST_TELE_TAN)).willReturn(true);
    given(this.tanService.getEntityByTan(TEST_TELE_TAN)).willReturn(Optional.of(getTeleTanTestData()));

    mockMvc.perform(post(PREFIX_API_VERSION + REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.registrationToken").exists());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals("Verification Failed: Amount of verification entries is not 1 (Result=" + count + "). ", 1, count);

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertNull(verificationList.get(0).getHashedGuid());
    assertEquals(AppSessionSourceOfTrust.TELETAN, verificationList.get(0).getSourceOfTrust());
    assertNotNull(verificationList.get(0).getRegistrationTokenHash());
  }

  /**
   * Test get registration token by a unknown Tele-Tan.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByUnknownTeleTan() throws Exception {
    log.info("process callGetRegistrationTokenByUnknownTeleTan() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TEST_TELE_TAN, RegistrationTokenKeyType.TELETAN);
    given(this.tanService.verifyTeleTan(TEST_TELE_TAN)).willReturn(Boolean.FALSE);

    mockMvc.perform(post(PREFIX_API_VERSION + REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(request)))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test get registration token by invalid Guid.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByInvalidHashedGUID() throws Exception {
    log.info("process callGetRegistrationTokenByInvalidHashedGUID() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TEST_INVALID_GUI_HASH, RegistrationTokenKeyType.GUID);

    mockMvc.perform(post(PREFIX_API_VERSION + REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(request)))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test get registration token for a guid, but the guid already has a registration token.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByAlreadyExistForGUID() throws Exception {
    log.info("process callGetRegistrationTokenByAlreadyExistForGUID() ");
    prepareAppSessionTestData();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TEST_GUI_HASH, RegistrationTokenKeyType.GUID);

    mockMvc.perform(post(PREFIX_API_VERSION + REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(request)))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test get registration token for a teletan, but the teletan already has a registration token.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByAlreadyExistForTeleTan() throws Exception {
    log.info("process callGetRegistrationTokenByAlreadyExistForTeleTan() ");

    appSessionrepository.deleteAll();
    VerificationAppSession appSessionTestData = getAppSessionTestData();
    appSessionTestData.setTeleTanHash(TEST_TELE_TAN_HASH);
    appSessionrepository.save(appSessionTestData);

    given(this.tanService.verifyTeleTan(TEST_TELE_TAN)).willReturn(true);

    RegistrationTokenRequest request = new RegistrationTokenRequest(TEST_TELE_TAN, RegistrationTokenKeyType.TELETAN);

    mockMvc.perform(post(PREFIX_API_VERSION + REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(request)))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test getTestState.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetTestState() throws Exception {
    log.info("process callGetTestState()");

    prepareAppSessionTestData();

    given(this.testResultServerService.result(new HashedGuid(TEST_GUI_HASH))).willReturn(TEST_LAB_POSITIVE_RESULT);

    mockMvc.perform(post(PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.testResult").value(TEST_LAB_POSITIVE_RESULT.getTestResult()));
  }

  /**
   * Test getTestState with empty Entity of VerificationAppSession.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetTestStateByAppSessionIsEmpty() throws Exception {
    log.info("process callGetTestStateByAppSessionIsEmpty()");

    //clean the repo
    appSessionrepository.deleteAll();

    mockMvc.perform(post(PREFIX_API_VERSION + "/testresult")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test verifyTAN.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTAN() throws Exception {
    log.info("process callVerifyTAN()");

    given(this.tanService.getEntityByTan(TEST_TAN)).willReturn(Optional.of(getVerificationTANTestData()));

    Optional<VerificationTan> verificationTan = this.tanService.getEntityByTan(TEST_TAN);
    assertFalse("Is TAN redeemed?", verificationTan
      .map(VerificationTan::isRedeemed).orElse(true));

    mockMvc.perform(post(PREFIX_API_VERSION + TAN_VERIFICATION_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isOk());
  }

  /**
   * Test verifyTAN with empty Entity.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTANByVerificationTANIsEmpty() throws Exception {
    log.info("process callVerifyTANByVerificationTANIsEmpty()");

    // without mock tanService.getEntityByTan so this method will return empty entity
    mockMvc.perform(post(PREFIX_API_VERSION + TAN_VERIFICATION_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isNotFound());
  }

  /**
   * Test verifyTAN with syntax problems.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTANByTanWithInvalidSyntax() throws Exception {
    log.info("process callVerifyTANByTanWithInvalidSyntax()");

    mockMvc.perform(post(PREFIX_API_VERSION + TAN_VERIFICATION_URI).contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new Tan(TEST_INVALID_TAN))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test verifyTAN expired from.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTANByExpiredTimeFrom() throws Exception {
    log.info("process callVerifyTANByExpiredTimeFrom()");

    VerificationTan cvtan = getVerificationTANTestData();
    // setValidFrom later 2 days then now
    cvtan.setValidFrom(LocalDateTime.now().plusDays(2));
    given(this.tanService.getEntityByTan(TEST_TAN)).willReturn(Optional.of(cvtan));

    mockMvc.perform(post(PREFIX_API_VERSION + TAN_VERIFICATION_URI).contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isNotFound());
  }

  /**
   * Test verifyTAN expired until.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTANByExpiredTimeUntil() throws Exception {
    log.info("process callVerifyTANByExpiredTimeUntil()");

    VerificationTan cvtan = getVerificationTANTestData();
    // setValidUntil earlier 2 days then now
    cvtan.setValidUntil(LocalDateTime.now().minusDays(2));
    given(this.tanService.getEntityByTan(TEST_TAN)).willReturn(Optional.of(cvtan));

    mockMvc.perform(post(PREFIX_API_VERSION + TAN_VERIFICATION_URI).contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isNotFound());
  }

  /**
   * Test verifyTAN is redeemed.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTANByIsRedeemed() throws Exception {
    log.info("process callVerifyTANByIsRedeemed()");

    VerificationTan cvtan = getVerificationTANTestData();
    // tan is redeemed
    cvtan.setRedeemed(true);
    given(this.tanService.getEntityByTan(TEST_TAN)).willReturn(Optional.of(cvtan));

    mockMvc.perform(post(PREFIX_API_VERSION + TAN_VERIFICATION_URI).contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isNotFound());
  }

  private String getJwtTestData(final long expirationSecondsToAdd, PrivateKey privateKey, AuthorizationRole... roles) throws UnsupportedEncodingException {
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

  private void prepareAppSessionTestData() {
    appSessionrepository.deleteAll();
    appSessionrepository.save(getAppSessionTestData());
  }

  private VerificationAppSession getAppSessionTestData() {
    VerificationAppSession cv = new VerificationAppSession();
    cv.setHashedGuid(TEST_GUI_HASH);
    cv.setCreatedAt(LocalDateTime.now());
    cv.setUpdatedAt(LocalDateTime.now());
    cv.setTanCounter(0);
    cv.setSourceOfTrust(AppSessionSourceOfTrust.HASHED_GUID);
    cv.setRegistrationTokenHash(TEST_REG_TOK_HASH);
    return cv;
  }

  private VerificationTan getVerificationTANTestData() {
    VerificationTan cvtan = new VerificationTan();
    cvtan.setCreatedAt(LocalDateTime.now());
    cvtan.setUpdatedAt(LocalDateTime.now());
    cvtan.setRedeemed(false);
    cvtan.setSourceOfTrust(TEST_SOT);
    cvtan.setTanHash(TEST_HASHED_TAN);
    cvtan.setType(TEST_TAN_TYPE);
    cvtan.setValidFrom(LocalDateTime.now().minusDays(5));
    cvtan.setValidUntil(TAN_VALID_UNTIL_IN_DAYS);
    return cvtan;
  }

  private VerificationTan getTeleTanTestData() {
    VerificationTan cvtan = new VerificationTan();
    cvtan.setCreatedAt(LocalDateTime.now());
    cvtan.setUpdatedAt(LocalDateTime.now());
    cvtan.setRedeemed(false);
    cvtan.setSourceOfTrust(TanSourceOfTrust.TELETAN);
    cvtan.setTanHash(TEST_HASHED_TAN);
    cvtan.setType(TanType.TELETAN.name());
    cvtan.setValidFrom(LocalDateTime.now());
    cvtan.setValidUntil(LocalDateTime.now().plusHours(1));
    return cvtan;
  }

  private String getAsJsonFormat(Object o) throws JsonProcessingException {
    return mapper.writeValueAsString(o);
  }
}
