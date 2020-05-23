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

import app.coronawarn.verification.client.TestResult;
import app.coronawarn.verification.client.HashedGuid;
import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.RegistrationTokenKeyType;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.model.Tan;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.model.TanType;
import app.coronawarn.verification.repository.VerificationAppSessionRepository;
import app.coronawarn.verification.service.LabServerService;
import app.coronawarn.verification.service.TanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(classes = VerificationApplication.class)
public class VerificationAppTests {

  public static final String TEST_GUI_HASH = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
  public static final String TEST_TELETAN = "8eFjPSV";
  public static final String TEST_REG_TOK = "1234567890";
  public static final String TEST_REG_TOK_HASH = "c775e7b757ede630cd0aa1113bd102661ab38829ca52a6422ab782862f268646";
  public static final TestResult TEST_LAB_POSITIVE_RESULT = new TestResult(2);
  public static final String TEST_TAN = "1ea6ce8a-9740-11ea-bb37-0242ac130002";
  public static final String TEST_SOT = "connectedLab17";
  public static final String TEST_HASHED_TAN = "16154ea91c2c59d6ef9d0e7f902a59283b1e7ff9111570d20139a4e6b1832876";
  public static final String TEST_TAN_TYPE = "TAN";
  static final Logger LOG = LogManager.getLogger();
  private static final LocalDateTime TAN_VALID_UNTIL_IN_DAYS = LocalDateTime.now().plusDays(7);
  private static final String PREFIX_API_VERSION = "/version/v1";

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private LabServerService labServerService;
  @MockBean
  private TanService tanService;
  @Autowired
  private VerificationAppSessionRepository appSessionrepository;
  @Autowired
  private ObjectMapper mapper;

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
  public void callGenerateTAN() throws Exception {
    log.info("VerificationAppTests callGenerateTAN()");

    prepareAppSessionTestData();
    doReturn(TEST_LAB_POSITIVE_RESULT).when(labServerService).result(any());

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
      .andExpect(status().isCreated());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals("Verification Failed: Amount of verfication entries is not 1 (Result=" + count + "). ", 1, count);

    List<VerificationAppSession> verficationList = appSessionrepository.findAll();
    assertNotNull(verficationList);
    assertEquals(TEST_GUI_HASH, verficationList.get(0).getHashedGuid());
    Assert.assertEquals(AppSessionSourceOfTrust.HASHED_GUID.getSourceName(), verficationList.get(0).getSourceOfTrust());
    assertEquals(TEST_REG_TOK_HASH, verficationList.get(0).getRegistrationTokenHash());

  }

  /**
   * Test get registration token by a guid.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByGuid() throws Exception {
    log.info("VerificationAppTests callGetRegistrationToken() ");

    RegistrationTokenRequest request = new RegistrationTokenRequest(TEST_GUI_HASH, RegistrationTokenKeyType.GUID);
    mockMvc.perform(post(PREFIX_API_VERSION + "/registrationToken")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.registrationToken").exists());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals("Verification Failed: Amount of verfication entries is not 1 (Result=" + count + "). ", 1, count);

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertEquals(TEST_GUI_HASH, verificationList.get(0).getHashedGuid());
    assertEquals(AppSessionSourceOfTrust.HASHED_GUID.getSourceName(), verificationList.get(0).getSourceOfTrust());
    assertNotNull(verificationList.get(0).getRegistrationTokenHash());
  }

  /**
   * Test get registration token by a tele tan.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByTeleTan() throws Exception {
    log.info("VerificationAppTests callGetRegistrationToken() ");
    appSessionrepository.deleteAll();
    RegistrationTokenRequest request = new RegistrationTokenRequest(TEST_TELETAN, RegistrationTokenKeyType.TELETAN);
    given(this.tanService.verifyTeleTan(TEST_TELETAN)).willReturn(true);
    given(this.tanService.isTeleTanValid(TEST_TELETAN)).willReturn(true);
    given(this.tanService.getEntityByTan(TEST_TELETAN)).willReturn(Optional.of(getTeleTanTestData()));

    mockMvc.perform(post(PREFIX_API_VERSION + "/registrationToken")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getAsJsonFormat(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.registrationToken").exists());

    long count = appSessionrepository.count();
    log.info("Got {} verification entries from db repository.", count);
    assertEquals("Verification Failed: Amount of verfication entries is not 1 (Result=" + count + "). ", 1, count);

    List<VerificationAppSession> verificationList = appSessionrepository.findAll();
    assertNotNull(verificationList);
    assertNull(verificationList.get(0).getHashedGuid());
    assertEquals(AppSessionSourceOfTrust.TELETAN.getSourceName(), verificationList.get(0).getSourceOfTrust());
    assertNotNull(verificationList.get(0).getRegistrationTokenHash());
  }

  /**
   * Test getTestState.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetTestState() throws Exception {
    log.info("VerificationAppTests callGetTestState()");

    prepareAppSessionTestData();

    given(this.labServerService.result(new HashedGuid(TEST_GUI_HASH))).willReturn(TEST_LAB_POSITIVE_RESULT);

    mockMvc.perform(post(PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON).content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
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
    log.info("VerificationAppTests callGetTestStateByAppSessionIsEmpty()");

    //clean the repo
    appSessionrepository.deleteAll();

    mockMvc.perform(post(PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON).content(getAsJsonFormat(new RegistrationToken(TEST_REG_TOK))))
      .andExpect(status().isBadRequest());
  }

  /**
   * Test verifyTAN.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTAN() throws Exception {
    log.info("VerificationAppTests callVerifyTAN()");

    given(this.tanService.syntaxVerification(TEST_TAN)).willReturn(true);
    given(this.tanService.getEntityByTan(TEST_TAN)).willReturn(Optional.of(getVerificationTANTestData()));

    assertFalse("Is TAN redeemed?", this.tanService.getEntityByTan(TEST_TAN).get().isRedeemed());

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan/verify").contentType(MediaType.APPLICATION_JSON).content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isOk());

    assertTrue("Is TAN redeemed?", this.tanService.getEntityByTan(TEST_TAN).get().isRedeemed());
  }

  /**
   * Test verifyTAN with empty Entity.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTANByVerificationTANIsEmpty() throws Exception {
    log.info("VerificationAppTests callVerifyTANByVerificationTANIsEmpty()");

    given(this.tanService.syntaxVerification(TEST_TAN)).willReturn(true);
    // without mock tanService.getEntityByTan so this method will return empty entity

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan/verify").contentType(MediaType.APPLICATION_JSON).content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isNotFound());
  }

  /**
   * Test verifyTAN with syntax problems.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTANByTanSyntaxFailed() throws Exception {
    log.info("VerificationAppTests callVerifyTANByTanSyntaxFailed()");

    // without mock tanService.syntaxVerification so this method will return false
    given(this.tanService.getEntityByTan(TEST_TAN)).willReturn(Optional.of(getVerificationTANTestData()));

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan/verify").contentType(MediaType.APPLICATION_JSON).content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isNotFound());
  }

  /**
   * Test verifyTAN expired from.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTANByExpiredTimeFrom() throws Exception {
    log.info("VerificationAppTests callVerifyTANByTanSyntaxFailed()");

    given(this.tanService.syntaxVerification(TEST_TAN)).willReturn(true);
    VerificationTan cvtan = getVerificationTANTestData();
    // setValidFrom later 2 days then now
    cvtan.setValidFrom(LocalDateTime.now().plusDays(2));
    given(this.tanService.getEntityByTan(TEST_TAN)).willReturn(Optional.of(cvtan));

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan/verify").contentType(MediaType.APPLICATION_JSON).content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isNotFound());
  }

  /**
   * Test verifyTAN expired until.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTANByExpiredTimeUntil() throws Exception {
    log.info("VerificationAppTests callVerifyTANByTanSyntaxFailed()");

    given(this.tanService.syntaxVerification(TEST_TAN)).willReturn(true);
    VerificationTan cvtan = getVerificationTANTestData();
    // setValidUntil earlier 2 days then now
    cvtan.setValidUntil(LocalDateTime.now().minusDays(2));
    given(this.tanService.getEntityByTan(TEST_TAN)).willReturn(Optional.of(cvtan));

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan/verify").contentType(MediaType.APPLICATION_JSON).content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isNotFound());
  }

  /**
   * Test verifyTAN is redeemed.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTANByIsRedeemed() throws Exception {
    log.info("VerificationAppTests callVerifyTANByIsRedeemed()");

    given(this.tanService.syntaxVerification(TEST_TAN)).willReturn(true);
    VerificationTan cvtan = getVerificationTANTestData();
    // tan is redeemed
    cvtan.setRedeemed(true);
    given(this.tanService.getEntityByTan(TEST_TAN)).willReturn(Optional.of(cvtan));

    mockMvc.perform(post(PREFIX_API_VERSION + "/tan/verify").contentType(MediaType.APPLICATION_JSON).content(getAsJsonFormat(new Tan(TEST_TAN))))
      .andExpect(status().isNotFound());
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
    cv.setSourceOfTrust(AppSessionSourceOfTrust.HASHED_GUID.getSourceName());
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
    cvtan.setSourceOfTrust(TanSourceOfTrust.TELETAN.getSourceName());
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
