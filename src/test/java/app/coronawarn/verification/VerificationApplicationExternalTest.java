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

import static app.coronawarn.verification.TestUtils.LAB_ID;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.verification.config.VerificationApplicationConfig;
import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.RegistrationTokenKeyType;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.model.TeleTanType;
import app.coronawarn.verification.repository.VerificationAppSessionRepository;
import app.coronawarn.verification.service.TanService;
import app.coronawarn.verification.service.TestResultServerService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * This is the test class for the verification application.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = VerificationApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles({"external","local"})
public class VerificationApplicationExternalTest {

  private static final String TOKEN_PADDING = "1";
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

  private static long cachedRequestSize;

  @BeforeEach
  public void setUp() {
    // Store max request size config property to allow tests to modify it
    cachedRequestSize = verificationApplicationConfig.getRequest().getSizelimit();
  }

  @AfterEach
  public void tearDown() {
    // Reset max request size to cached value
    verificationApplicationConfig.getRequest().setSizelimit(cachedRequestSize);
  }

  /**
   * Test generateTAN.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTanWithFake() throws Exception {
    log.info("process callGenerateTan()");

    TestUtils.prepareAppSessionTestData(appSessionrepository);
    doReturn(TestUtils.TEST_LAB_POSITIVE_RESULT).when(testResultServerService).result(any());

    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .secure(true)
      .header("cwa-fake", "1")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING)))).andReturn();
    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isCreated());
  }

  /**
   * Test generateTANForQuickTest.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTan() throws Exception {
    log.info("process callGenerateTan()");

    TestUtils.prepareAppSessionTestData(appSessionrepository);
    doReturn(TestUtils.TEST_LAB_POSITIVE_RESULT).when(testResultServerService).result(any());
    doReturn(TestUtils.TEST_TAN).when(tanService).generateVerificationTan(any(), any());

    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .secure(true)
      .header("cwa-fake", "0")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isCreated());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals(1, count, "Verification Failed: Amount of verfication entries is not 1 (Result=" + count + "). ");

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertEquals(TestUtils.TEST_GUI_HASH, verificationList.get(0).getHashedGuid());
    assertEquals(AppSessionSourceOfTrust.HASHED_GUID, verificationList.get(0).getSourceOfTrust());
    assertEquals(TestUtils.TEST_REG_TOK_HASH, verificationList.get(0).getRegistrationTokenHash());
    assertEquals(TeleTanType.EVENT, verificationList.get(0).getTeleTanType());

  }

  @Test
  public void callGenerateTanForQuickTest() throws Exception {
    log.info("process callGenerateTanForQuickTest()");

    TestUtils.prepareAppSessionTestData(appSessionrepository);
    doReturn(TestUtils.QUICK_TEST_POSITIVE_RESULT).when(testResultServerService).result(any());
    doReturn(TestUtils.TEST_TAN).when(tanService).generateVerificationTan(any(), any());

    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .secure(true)
      .header("cwa-fake", "0")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isCreated());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals(1, count, "Verification Failed: Amount of verfication entries is not 1 (Result=" + count + "). ");

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
    appSessionrepository.deleteAll();
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "0")
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
      .secure(true)
      .header("cwa-fake", "0")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_INVALID_REG_TOK, TOKEN_PADDING))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test generateTAN with an fake test result from the lab-server.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTanWithNegativeCovidResultFaked() throws Exception {
    log.info("process callGenerateTanWithNegativeCovidResult()");
    TestUtils.prepareAppSessionTestData(appSessionrepository);
    doReturn(TestUtils.TEST_LAB_NEGATIVE_RESULT).when(testResultServerService).result(any());

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .secure(true)
      .header("cwa-fake", "1")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
      .andExpect(status().isOk());
  }

  /**
   * Test generateTAN with an negative test result from the lab-server.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTanWithNegativeCovidResultWithoutFakeHeader() throws Exception {
    log.info("process callGenerateTanWithNegativeCovidResult()");
    TestUtils.prepareAppSessionTestData(appSessionrepository);
    doReturn(TestUtils.TEST_LAB_NEGATIVE_RESULT).when(testResultServerService).result(any());
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
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
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
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
      .secure(true)
      .header("cwa-fake", "0")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
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
    doReturn(TestUtils.TEST_TELE_TAN).when(tanService).generateVerificationTan(any(), any());

    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan")
      .secure(true)
      .header("cwa-fake", "0")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
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
      .secure(true)
      .header("cwa-fake", "0")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, null, RegistrationTokenKeyType.GUID);
    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "0")
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isCreated());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals(1, count, "Verification Failed: Amount of verfication entries is not 1 (Result=" + count + "). ");

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertEquals(TestUtils.TEST_GUI_HASH, verificationList.get(0).getHashedGuid());
    assertEquals(AppSessionSourceOfTrust.HASHED_GUID, verificationList.get(0).getSourceOfTrust());
    assertNotNull(verificationList.get(0).getRegistrationTokenHash());
  }

  /**
   * Test get registration token by a guid.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByGuidAndDobHash() throws Exception {
    log.info("process callGetRegistrationTokenByGuid() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, TestUtils.TEST_GUI_HASH_DOB, RegistrationTokenKeyType.GUID);
    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "0")
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isCreated());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals(1, count, "Verification Failed: Amount of verfication entries is not 1 (Result=" + count + "). ");

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertEquals(TestUtils.TEST_GUI_HASH, verificationList.get(0).getHashedGuid());
    assertEquals(TestUtils.TEST_GUI_HASH_DOB, verificationList.get(0).getHashedGuidDob());
    assertEquals(AppSessionSourceOfTrust.HASHED_GUID, verificationList.get(0).getSourceOfTrust());
    assertNotNull(verificationList.get(0).getRegistrationTokenHash());
  }

  @Test
  public void callGetRegistrationTokenByGuidAndDobHashAndWithoutDobHashAfterwards() throws Exception {
    log.info("process callGetRegistrationTokenByGuid() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, TestUtils.TEST_GUI_HASH_DOB, RegistrationTokenKeyType.GUID);
    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "0")
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isCreated());

    request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, null, RegistrationTokenKeyType.GUID);
    result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "0")
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void callGetRegistrationTokenByGuidAndDobHashAndWithAnotherDobHashAfterwards() throws Exception {
    log.info("process callGetRegistrationTokenByGuid() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, TestUtils.TEST_GUI_HASH_DOB, RegistrationTokenKeyType.GUID);
    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "0")
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isCreated());

    request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, TestUtils.TEST_GUI_HASH_DOB2, RegistrationTokenKeyType.GUID);
    result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "0")
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void callGetRegistrationTokenByGuidAndDobHashAndUseDobHashAgainAfterwards() throws Exception {
    log.info("process callGetRegistrationTokenByGuid() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, TestUtils.TEST_GUI_HASH_DOB, RegistrationTokenKeyType.GUID);
    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "0")
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isCreated());

    request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH2, TestUtils.TEST_GUI_HASH_DOB, RegistrationTokenKeyType.GUID);
    result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "0")
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void callGetRegistrationTokenByDobHash() throws Exception {
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH_DOB, null, RegistrationTokenKeyType.GUID);
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "0")
      .content(TestUtils.getAsJsonFormat(request)))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test get registration token by a guid with fake.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByGuidWithFake() throws Exception {
    log.info("process callGetRegistrationTokenByGuid() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, null, RegistrationTokenKeyType.GUID);
    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .header("cwa-fake", "1")
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();
    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isCreated());

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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, null, null);
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .header("cwa-fake", "0")
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(null, null, RegistrationTokenKeyType.GUID);
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .header("cwa-fake", "0")
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_TELE_TAN, null, RegistrationTokenKeyType.TELETAN);
    given(this.tanService.verifyTeleTan(TestUtils.TEST_TELE_TAN)).willReturn(true);
    given(this.tanService.isTeleTanValid(TestUtils.TEST_TELE_TAN)).willReturn(true);
    given(this.tanService.getEntityByTan(TestUtils.TEST_TELE_TAN)).willReturn(Optional.of(TestUtils.getTeleTanTestData()));

    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();
    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isCreated());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals(1, count, "Verification Failed: Amount of verfication entries is not 1 (Result=" + count + "). ");

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertNull(verificationList.get(0).getHashedGuid());
    assertEquals(AppSessionSourceOfTrust.TELETAN, verificationList.get(0).getSourceOfTrust());
    assertNotNull(verificationList.get(0).getRegistrationTokenHash());
    assertEquals(TeleTanType.EVENT, verificationList.get(0).getTeleTanType());
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_TELE_TAN, null, RegistrationTokenKeyType.TELETAN);
    given(this.tanService.verifyTeleTan(TestUtils.TEST_TELE_TAN)).willReturn(false);
    given(this.tanService.getEntityByTan(TestUtils.TEST_TELE_TAN)).willReturn(Optional.empty());

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .header("cwa-fake", "0")
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_INVALID_GUI_HASH, null, RegistrationTokenKeyType.GUID);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .header("cwa-fake", "0")
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
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, null, RegistrationTokenKeyType.GUID);

    MvcResult result = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .header("cwa-fake", "0")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
      .andReturn();
    mockMvc.perform(asyncDispatch(result))
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

    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_TELE_TAN, null, RegistrationTokenKeyType.TELETAN);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .header("cwa-fake", "0")
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

    MvcResult mvcResult = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
      .andReturn();

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk())
      .andExpect(jsonPath("sc").exists())
      .andExpect(jsonPath("$.labId", is(LAB_ID)));
  }

  @Test
  public void callGetTestStateWithDobRegistrationToken() throws Exception {
    TestUtils.prepareAppSessionTestDataDob(appSessionrepository);

    given(this.testResultServerService.result(new HashedGuid(TestUtils.TEST_GUI_HASH))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);
    given(this.testResultServerService.result(new HashedGuid(TestUtils.TEST_GUI_HASH_DOB))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);

    MvcResult mvcResult = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
      .andReturn();

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk())
      .andExpect(jsonPath("sc").exists())
      .andExpect(jsonPath("$.labId", is(LAB_ID)));
  }

  @Test
  public void callGetTestStateWithDobRegistrationTokenAndTrsRespondsWithDifferentResults() throws Exception {
    TestUtils.prepareAppSessionTestDataDob(appSessionrepository);

    given(this.testResultServerService.result(new HashedGuid(TestUtils.TEST_GUI_HASH))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);
    given(this.testResultServerService.result(new HashedGuid(TestUtils.TEST_GUI_HASH_DOB))).willReturn(TestUtils.TEST_LAB_NEGATIVE_RESULT);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
      .andExpect(status().isForbidden());
  }

  /**
   * Test getTestState fake.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetTestStateWithFake() throws Exception {
    log.info("process callGetTestState()");

    TestUtils.prepareAppSessionTestData(appSessionrepository);

    given(this.testResultServerService.result(new HashedGuid(TestUtils.TEST_GUI_HASH))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);

    MvcResult mvcResult = mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .header("cwa-fake", "1")
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
      .andReturn();

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.labId").doesNotExist())
      .andExpect(jsonPath("$.sc").exists());
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
      .secure(true)
      .header("cwa-fake", "0")
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test that the endpoint for verifying tans is not reachable when external profile is activated.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void testVerifyEndpointShouldNotBeReachable() throws Exception {
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.TAN_VERIFICATION_URI)
      .secure(true)
      .header("cwa-fake", "0"))
      .andExpect(status().isNotFound());
  }

  /**
   * Test that the endpoint for creating new TeleTans is not reachable when external profile is activated.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void testTeleTanEndpointShouldNotBeReachable() throws Exception {
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan/teletan").header("cwa-fake", "0")
      .secure(true)
    )
      .andExpect(status().isNotFound());
  }

  /**
   * Test get registration token by a guid with a size larger than the maxSizeLimit.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByLargeRequest() throws Exception {
    log.info("process callGetRegistrationTokenByLargeRequest() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TestUtils.TEST_GUI_HASH, null, RegistrationTokenKeyType.GUID);
    //Set the maxSizeLimit to 10 for testing
    verificationApplicationConfig.getRequest().setSizelimit(10);
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(request)))
      .andExpect(status().isNotAcceptable());
  }

}
