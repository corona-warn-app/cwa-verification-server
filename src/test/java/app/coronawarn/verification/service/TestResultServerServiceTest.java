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

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.verification.client.TestResultServerClient;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
public class TestResultServerServiceTest {

  public static final String TEST_GUI_HASH_1 = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
  public static final String TEST_GUI_HASH_2 = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13c";
  private static final String TEST_RESULT_PADDING = "";
  public static final TestResult TEST_LAB_POSITIVE_RESULT = new TestResult(2, 0, null, null);
  public static final TestResult TEST_LAB_REDEEMED_RESULT = new TestResult(4, 0, null, null);
  private TestResultServerService testResultServerService;
 
  @BeforeEach
  public void setUp() {
    testResultServerService = new TestResultServerService(new TestResultServerClientMock());
  }

  /**
   * Test result method by positive status.
   */
  @Test
  public void resultPositive() {
    TestResult testResult = testResultServerService.result(new HashedGuid(TEST_GUI_HASH_1));
    assertThat(testResult).isEqualTo(TEST_LAB_POSITIVE_RESULT);
  }

  /**
   * Test result method by redeemed status.
   */
  @Test
  public void resultRedeemed() {
    TestResult testResult = testResultServerService.result(new HashedGuid(TEST_GUI_HASH_2));
    assertThat(testResult).isEqualTo(TEST_LAB_REDEEMED_RESULT);
  }

  public static class TestResultServerClientMock implements TestResultServerClient {
    @Override
    public TestResult result(HashedGuid guid) {
      if (guid.getId().equals(TEST_GUI_HASH_1)) {
        return new TestResult(2, 0, null, null);
      }
      return new TestResult(4, 0, null, null);
    }
  }
}
