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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HashingServiceTest {

  HashingService hashingService = new HashingService();

  @Test
  public void testValidSha256Hash() {
    assertTrue(hashingService.isHashValid("523463041ef9ffa2950d8450feb34c88bc8692c40c9cf3c99dcdf75e270229e2"));
    assertTrue(hashingService.isHashValid("0000000000000000000000000000000000000000000000000000000000000000"));
    assertTrue(hashingService.isHashValid("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
  }

  @Test
  public void testInvalidSha256Hash() {
    assertFalse(hashingService.isHashValid("x23463041ef9ffa2950d8z50feb34c88bc8692c40c9cf3c99dcdf75e270229e2"));
    assertFalse(hashingService.isHashValid("523463041ef9ffa2950d8z50feb34c88bc8692c40c9cf3c99dcdf75e270229e2"));
    assertFalse(hashingService.isHashValid("0"));
    assertFalse(hashingService.isHashValid("0000000000000000000000000000000000000000000000000000000000000000f"));
    assertFalse(hashingService.isHashValid("0000000000000000000000000000000000000000000000000000000000000000\n"));
  }

  @Test
  public void testGetCheckDigit() {
    assertThat(hashingService.getCheckDigit("FE9A5MAK6").equals("C"));
    assertThat(hashingService.getCheckDigit("WPHSATMHD").equals("4"));
    assertThat(hashingService.getCheckDigit("9N4UTTACE").equals("6"));
    assertThat(hashingService.getCheckDigit("S3HHJJYJD").equals("3"));
    assertThat(hashingService.getCheckDigit("W3M75DUD7").equals("C"));
    assertThat(hashingService.getCheckDigit("BBA3M8UVU").equals("C"));
    assertThat(hashingService.getCheckDigit("MNSHDZAEJ").equals("2"));
    assertThat(hashingService.getCheckDigit("WS732AR8Q").equals("B"));
    // special cases
    assertThat(hashingService.getCheckDigit("FE9A5MAK9").equals("H"));
    assertThat(hashingService.getCheckDigit("FE9A5MAKW").equals("G"));
  }
}
