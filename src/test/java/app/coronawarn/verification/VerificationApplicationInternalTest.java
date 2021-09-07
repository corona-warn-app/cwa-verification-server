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
import static app.coronawarn.verification.TestUtils.TEST_GUI_HASH;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.verification.controller.InternalTanController;
import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.model.AuthorizationRole;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.Tan;
import app.coronawarn.verification.model.TeleTanType;
import app.coronawarn.verification.repository.VerificationAppSessionRepository;
import app.coronawarn.verification.service.JwtService;
import app.coronawarn.verification.service.TanService;
import app.coronawarn.verification.service.TestResultServerService;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
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

/**
 * This is the test class for the verification application.
 */
@ActiveProfiles({"internal","local"})
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "disable-dob-hash-check-for-external-test-result=true")
@ContextConfiguration(classes = VerificationApplication.class)
@AutoConfigureMockMvc

public class VerificationApplicationInternalTest {

  private static final String TAN_PADDING = "";
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private TanService tanService;
  @MockBean
  private JwtService jwtService;

  @Autowired
  private VerificationAppSessionRepository appSessionRepository;

  @MockBean
  private TestResultServerService testResultServerServiceMock;

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
    String jwtString = TestUtils.getJwtTestData(3000, kp.getPrivate(), AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);

    given(this.tanService.isTeleTanRateLimitNotExceeded()).willReturn(Boolean.TRUE);
    given(this.jwtService.isAuthorized(any(), any())).willReturn(Boolean.TRUE);
    given(this.jwtService.getPublicKey()).willReturn(kp.getPublic());
    when(this.jwtService.validateToken(jwtString, kp.getPublic(), Collections.emptyList())).thenCallRealMethod();

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan/teletan").header(JwtService.HEADER_NAME_AUTHORIZATION, JwtService.TOKEN_PREFIX + jwtString).secure(true))
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
    given(this.jwtService.isAuthorized(any(), any())).willReturn(false);
    given(this.jwtService.getPublicKey()).willReturn(kp.getPublic());
    String jwtString = TestUtils.getJwtTestData(3000, kp.getPrivate(), AuthorizationRole.AUTH_C19_HEALTHAUTHORITY);
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan/teletan").header(JwtService.HEADER_NAME_AUTHORIZATION,
      JwtService.TOKEN_PREFIX + jwtString).secure(true))
      .andExpect(status().isUnauthorized());
  }

  /**
   * Test verifyTAN.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callVerifyTAN() throws Exception {
    log.info("process callVerifyTAN()");

    given(this.tanService.getEntityByTan(TestUtils.TEST_TAN)).willReturn(Optional.of(TestUtils.getVerificationTANTestData()));

    Optional<VerificationTan> verificationTan = this.tanService.getEntityByTan(TestUtils.TEST_TAN);
    Assertions.assertFalse(verificationTan.map(VerificationTan::isRedeemed).orElse(true));

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.TAN_VERIFICATION_URI)
      .contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new Tan(TestUtils.TEST_TAN, TAN_PADDING))))
      .andExpect(status().isOk())
      .andExpect(header().string(InternalTanController.TELE_TAN_TYPE_HEADER, TeleTanType.EVENT.toString()));
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
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.TAN_VERIFICATION_URI)
      .secure(true)
      .contentType(MediaType.APPLICATION_JSON)
      .content(TestUtils.getAsJsonFormat(new Tan(TestUtils.TEST_TAN, TAN_PADDING))))
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

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.TAN_VERIFICATION_URI).contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new Tan(TestUtils.TEST_INVALID_TAN, TAN_PADDING))))
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

    VerificationTan cvtan = TestUtils.getVerificationTANTestData();
    // setValidFrom later 2 days then now
    cvtan.setValidFrom(LocalDateTime.now().plusDays(2));
    given(this.tanService.getEntityByTan(TestUtils.TEST_TAN)).willReturn(Optional.of(cvtan));

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.TAN_VERIFICATION_URI).contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new Tan(TestUtils.TEST_TAN, TAN_PADDING))))
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

    VerificationTan cvtan = TestUtils.getVerificationTANTestData();
    // setValidUntil earlier 2 days then now
    cvtan.setValidUntil(LocalDateTime.now().minusDays(2));
    given(this.tanService.getEntityByTan(TestUtils.TEST_TAN)).willReturn(Optional.of(cvtan));

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.TAN_VERIFICATION_URI).contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .header("cwa-fake", 0)
      .content(TestUtils.getAsJsonFormat(new Tan(TestUtils.TEST_TAN, TAN_PADDING))))
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

    VerificationTan cvtan = TestUtils.getVerificationTANTestData();
    // tan is redeemed
    cvtan.setRedeemed(true);
    given(this.tanService.getEntityByTan(TestUtils.TEST_TAN)).willReturn(Optional.of(cvtan));

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.TAN_VERIFICATION_URI).contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new Tan(TestUtils.TEST_TAN, TAN_PADDING))))
      .andExpect(status().isNotFound());
  }

  /**
   * Test that the endpoint for generating tans is not reachable when internal profile is activated.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGenerateTan() throws Exception {
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan").secure(true))
      .andExpect(status().isNotFound());
  }


  /**
   * Test that the endpoint for registration tokens is not reachable when internal profile is activated.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetRegistrationTokenByGuid() throws Exception {
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + TestUtils.REGISTRATION_TOKEN_URI).secure(true))
      .andExpect(status().isNotFound());
  }

  @Test
  public void shouldReturn429StatusCodeIfRateLimitIsExceeded() throws Exception {
    given(this.jwtService.isAuthorized(any(), any())).willReturn(Boolean.TRUE);
    given(this.tanService.isTeleTanRateLimitNotExceeded()).willReturn(Boolean.TRUE);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan/teletan").header(JwtService.HEADER_NAME_AUTHORIZATION, "").secure(true))
      .andExpect(status().isCreated());

    given(this.tanService.isTeleTanRateLimitNotExceeded()).willReturn(Boolean.FALSE);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/tan/teletan").header(JwtService.HEADER_NAME_AUTHORIZATION, "").secure(true))
      .andExpect(status().isTooManyRequests());
  }

  /**
   * Test getTestState.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void callGetTestState() throws Exception {
    log.info("process callGetTestState()");

    TestUtils.prepareAppSessionTestData(appSessionRepository);

    given(testResultServerServiceMock.result(new HashedGuid(TestUtils.TEST_GUI_HASH))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, null))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.labId", is(LAB_ID)))
      .andExpect(jsonPath("$.testId", is(TEST_GUI_HASH)));

  }

  @Test
  public void callGetTestStateOfTeleTanTokenShouldFail() throws Exception {
    TestUtils.prepareAppSessionTestDataSotTeleTan(appSessionRepository);

    given(testResultServerServiceMock.result(new HashedGuid(TestUtils.TEST_GUI_HASH))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, null))))
      .andExpect(status().isForbidden());
  }

  @Test
  public void callGetTestStateOfInvalidTokenShouldFail() throws Exception {
    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken("1ea6ce8a-9740-41ea-bb37-0242ac1aaaaa", null))))
      .andExpect(status().isNotFound());
  }

  @Test
  public void callGetTestStateWithDobRegistrationToken() throws Exception {
    TestUtils.prepareAppSessionTestDataDob(appSessionRepository);

    given(this.testResultServerServiceMock.result(new HashedGuid(TestUtils.TEST_GUI_HASH))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);
    given(this.testResultServerServiceMock.result(new HashedGuid(TestUtils.TEST_GUI_HASH_DOB))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, null))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("sc").exists())
      .andExpect(jsonPath("$.labId", is(LAB_ID)))
      .andExpect(jsonPath("$.testId", is(TEST_GUI_HASH)));
  }

  @Test
  public void callGetTestStateWithDobRegistrationTokenAndTrsRespondsWithDifferentResults() throws Exception {
    TestUtils.prepareAppSessionTestDataDob(appSessionRepository);

    given(this.testResultServerServiceMock.result(new HashedGuid(TestUtils.TEST_GUI_HASH))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);
    given(this.testResultServerServiceMock.result(new HashedGuid(TestUtils.TEST_GUI_HASH_DOB))).willReturn(TestUtils.TEST_LAB_NEGATIVE_RESULT);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, null))))
      .andExpect(status().isForbidden());
  }

}
