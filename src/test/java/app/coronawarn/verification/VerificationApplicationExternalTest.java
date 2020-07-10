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
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.RegistrationTokenKeyType;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.repository.VerificationAppSessionRepository;
import app.coronawarn.verification.service.JwtService;
import app.coronawarn.verification.service.TanService;
import app.coronawarn.verification.service.TestResultServerService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("external")
public class VerificationApplicationExternalTest {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private TestResultServerService testResultServerService;
  @MockBean
  private TanService tanService;
  @Autowired
  private VerificationAppSessionRepository appSessionrepository;

  @Autowired
  private VerificationApplicationConfig verificationApplicationConfig;

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

    TestUtils.prepareAppSessionTestData(appSessionrepository);
    doReturn(TestUtils.TEST_LAB_POSITIVE_RESULT).when(testResultServerService).result(any());

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK))))
      .andExpect(status().isCreated());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals("Verification Failed: Amount of verification entries is not 1 (Result=" + count + "). ", 1, count);

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertEquals(TestUtils.TEST_GUI_HASH, verificationList.get(0).getHashedGuid());
    assertEquals(AppSessionSourceOfTrust.HASHED_GUID, verificationList.get(0).getSourceOfTrust());
    assertEquals(TestUtils.TEST_REG_TOK_HASH, verificationList.get(0).getRegistrationTokenHash());

  }

  /**
   * Test generateTAN with an unknown registration token.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTanByUnknownToken() throws Exception {
    log.info("process callGenerateTanByUnknownToken()");

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK))))
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

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_INVALID_REG_TOK))))
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
    TestUtils.prepareAppSessionTestData(appSessionrepository);
    doReturn(TestUtils.TEST_LAB_NEGATIVE_RESULT).when(testResultServerService).result(any());

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK))))
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
    VerificationAppSession appSessionTestData = TestUtils.getAppSessionTestData();
    int tancountermax = verificationApplicationConfig.getAppsession().getTancountermax();
    appSessionTestData.setTanCounter(tancountermax);
    appSessionrepository.save(appSessionTestData);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK))))
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
    VerificationAppSession appSessionTestData = TestUtils.getAppSessionTestData();
    appSessionTestData.setSourceOfTrust(AppSessionSourceOfTrust.TELETAN);
    appSessionrepository.save(appSessionTestData);
    doReturn(TestUtils.TEST_LAB_NEGATIVE_RESULT).when(testResultServerService).result(any());

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK))))
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
    VerificationAppSession appSessionTestData = TestUtils.getAppSessionTestData();
    appSessionTestData.setSourceOfTrust(AppSessionSourceOfTrust.HASHED_GUID);
    appSessionrepository.save(appSessionTestData);
    doReturn(TestUtils.TEST_LAB_NEGATIVE_RESULT).when(testResultServerService).result(any());

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK))))
      .andExpect(status().isBadRequest());
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, RegistrationTokenKeyType.GUID);
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.registrationToken").exists());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals("Verification Failed: Amount of verification entries is not 1 (Result=" + count + "). ", 1, count);

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertEquals(TestUtils.TEST_GUI_HASH, verificationList.get(0).getHashedGuid());
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, null);
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
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
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_TELE_TAN, RegistrationTokenKeyType.TELETAN);
    given(this.tanService.verifyTeleTan(TestUtils.TEST_TELE_TAN)).willReturn(true);
    given(this.tanService.isTeleTanValid(TestUtils.TEST_TELE_TAN)).willReturn(true);
    given(this.tanService.getEntityByTan(TestUtils.TEST_TELE_TAN)).willReturn(Optional.of(TestUtils.getTeleTanTestData()));

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_TELE_TAN, RegistrationTokenKeyType.TELETAN);
    given(this.tanService.verifyTeleTan(TestUtils.TEST_TELE_TAN)).willReturn(false);
    given(this.tanService.getEntityByTan(TestUtils.TEST_TELE_TAN)).willReturn(Optional.empty());

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_INVALID_GUI_HASH, RegistrationTokenKeyType.GUID);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
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
    TestUtils.prepareAppSessionTestData(appSessionrepository);
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, RegistrationTokenKeyType.GUID);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
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
    VerificationAppSession appSessionTestData = TestUtils.getAppSessionTestData();
    appSessionTestData.setTeleTanHash(TestUtils.TEST_TELE_TAN_HASH);
    appSessionrepository.save(appSessionTestData);

    given(this.tanService.verifyTeleTan(TestUtils.TEST_TELE_TAN)).willReturn(true);

    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_TELE_TAN, RegistrationTokenKeyType.TELETAN);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
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

    TestUtils.prepareAppSessionTestData(appSessionrepository);

    given(this.testResultServerService.result(new HashedGuid(TestUtils.TEST_GUI_HASH))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.testResult").value(TestUtils.TEST_LAB_POSITIVE_RESULT.getTestResult()));
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

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test that the endpoint for verifying tans is not reachable when external profile is activated.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void testVerifyEndpointShouldNotBeReachable() throws Exception {
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.TAN_VERIFICATION_URI))
      .andExpect(status().isNotFound());
  }

  /**
   * Test that the endpoint for creating new TeleTans is not reachable when external profile is activated.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void testTeleTanEndpointShouldNotBeReachable() throws Exception {
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan/teletan"))
      .andExpect(status().isNotFound());
  }

}
