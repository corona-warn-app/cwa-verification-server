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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VerificationApplication.class)
public class HashingServiceTest {

  @Autowired
  HashingService hashingService;

  @Test
  public void testValidSha256Hash() {
    String hash = "523463041ef9ffa2950d8450feb34c88bc8692c40c9cf3c99dcdf75e270229e2";
    boolean result = hashingService.isHashValid(hash);

    assertTrue(result);
  }

  @Test
  public void testInvalidSha256Hash() {
    String hash = "523463041ef9ffa2950d8z50feb34c88bc8692c40c9cf3c99dcdf75e270229e2";
    boolean result = hashingService.isHashValid(hash);

    assertFalse(result);
  }


}
