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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.repository.VerificationAppSessionRepository;
import app.coronawarn.verification.service.TestResultServerService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "disable-dob-hash-check-for-external-test-result=true")
@ContextConfiguration(classes = VerificationApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles({"external","local"})
public class VerificationApplicationExternalTestWorkaround {

  private static final String TOKEN_PADDING = "1";
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private TestResultServerService testResultServerService;
  @Autowired
  private VerificationAppSessionRepository appSessionrepository;

  @Test
  public void callGetTestStateWithDobRegistrationTokenAndTrsRespondsWithDifferentResults() throws Exception {
    TestUtils.prepareAppSessionTestDataDob(appSessionrepository);

    given(this.testResultServerService.result(new HashedGuid(TestUtils.TEST_GUI_HASH))).willReturn(TestUtils.TEST_LAB_POSITIVE_RESULT);
    given(this.testResultServerService.result(new HashedGuid(TestUtils.TEST_GUI_HASH_DOB))).willReturn(TestUtils.TEST_LAB_NEGATIVE_RESULT);

    mockMvc.perform(post(TestUtils.PREFIX_API_VERSION + "/testresult").contentType(MediaType.APPLICATION_JSON)
      .secure(true)
      .content(TestUtils.getAsJsonFormat(new RegistrationToken(TestUtils.TEST_REG_TOK, TOKEN_PADDING))))
      .andExpect(status().isOk());
  }

}
