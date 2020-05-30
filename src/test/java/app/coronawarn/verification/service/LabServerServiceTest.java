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
import app.coronawarn.verification.client.HashedGuid;
import app.coronawarn.verification.client.LabServerClient;
import app.coronawarn.verification.client.TestResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VerificationApplication.class)
public class LabServerServiceTest {

  public static final String TEST_GUI_HASH = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
  public static final TestResult TEST_LAB_POSITIVE_RESULT = new TestResult(2);
  private LabServerService labServerService;

  @Before
  public void setUp() {
    labServerService = new LabServerService(new LabServerClientMock());
  }

  /**
   * Test result method.
   */
  @Test
  public void resultTest() {
    HashedGuid hashedGuid = new HashedGuid(TEST_GUI_HASH);
    TestResult testResult = labServerService.result(hashedGuid);
    assertThat(testResult).isEqualTo(TEST_LAB_POSITIVE_RESULT);
  }

  public static class LabServerClientMock implements LabServerClient {
    @Override
    public TestResult result(HashedGuid guid) {
      return new TestResult(2);
    }
  }
}
