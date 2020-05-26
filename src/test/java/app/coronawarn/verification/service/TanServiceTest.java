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
import app.coronawarn.verification.domain.VerificationTan;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertFalse;
import org.junit.Assert;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VerificationApplication.class)
public class TanServiceTest {

  public static final String TEST_TAN = "1ea6ce8a-9740-11ea-bb37-0242ac130002";
  public static final String TEST_TAN_HASH = "8de76b627f0be70ea73c367a9a560d6a987eacec71f57ca3d86b2e4ed5b6f780";
  public static final String TEST_GUI_HASH = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
  public static final String TEST_TAN_TYPE = "TAN";
  public static final String VALID_TELE_TAN = "R3ZNUeV";
  private static final String TELETAN_PATTERN = "^[2-9A-HJ-KMNP-Za-kmnp-z]{7}$";
  private static final Pattern PATTERN = Pattern.compile(TELETAN_PATTERN);
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS");
  private static final LocalDateTime TAN_VALID_UNTIL_IN_DAYS = LocalDateTime.now().plusDays(7);

  @Autowired
  TanService tanService;

  /**
   * Test saveTan.
   *
   * @throws Exception if the test cannot be performed.
   */
  @Test
  public void saveTanTest() throws Exception {
    VerificationTan tan = new VerificationTan();
    tan.setCreatedAt(LocalDateTime.now());
    tan.setUpdatedAt(LocalDateTime.now());
    tan.setRedeemed(false);
    tan.setTanHash(TEST_GUI_HASH);
    tan.setValidFrom(LocalDateTime.now());
    tan.setValidUntil(TAN_VALID_UNTIL_IN_DAYS);
    tan.setType(TEST_TAN_TYPE);
    tan.setSourceOfTrust("");

    VerificationTan retunedTan = tanService.saveTan(tan);
    Assert.assertEquals(retunedTan, tan);
  }

  @Test
  public void getEntityByTanTest() {
    VerificationTan tan = new VerificationTan();
    LocalDateTime start = LocalDateTime.parse(LocalDateTime.now().format(FORMATTER));
    tan.setCreatedAt(start);
    tan.setUpdatedAt(start);
    tan.setRedeemed(false);
    tan.setTanHash(TEST_TAN_HASH);

    tan.setValidFrom(start);
    tan.setValidUntil(LocalDateTime.parse((TAN_VALID_UNTIL_IN_DAYS.format(FORMATTER))));
    tan.setType(TEST_TAN_TYPE);
    tan.setSourceOfTrust("");
    tanService.saveTan(tan);

    Optional<VerificationTan> tanFromDB = tanService.getEntityByTan(TEST_TAN);
    Assert.assertEquals(tan, tanFromDB.get());
  }

  @Test
  public void generateTeleTan() {
    String teleTan = tanService.generateTeleTan();
    Matcher matcher = PATTERN.matcher(teleTan);
    assertTrue(matcher.find());
  }

  @Test
  public void verifyTeletan() {
    String teleTan = tanService.generateVerificationTeleTan();
    assertTrue(tanService.checkTanAlreadyExist(VALID_TELE_TAN));
    assertTrue(tanService.verifyTeleTan(teleTan));
    assertFalse(tanService.verifyTeleTan("R3ZNUI0"));
  }

  @Test
  public void checkTanAlreadyExist() {
    assertTrue(tanService.checkTanAlreadyExist(VALID_TELE_TAN));
  }

  @Test
  public void generateVerificationTan() {
    String tan = tanService.generateVerificationTan("test");
    assertTrue(tanService.syntaxVerification(tan));
    assertFalse(tan.isEmpty());
  }

  @Test
  public void generateValidTan() {
    String tan = tanService.generateValidTan();
    assertTrue(tanService.syntaxVerification(tan));
    assertFalse(tan.isEmpty());
  }

  @Test
  public void testTeleTANFormat() {
    assertThat(tanService.isTeleTanValid("29zAE4E")).isTrue();
    assertThat(tanService.isTeleTanValid("29zAE4O")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAE40")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAE41")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAE4I")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAE4L")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAEil")).isFalse();
    assertThat(tanService.isTeleTanValid("29zA?ßö")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAE4EZ")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAE4")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAL4-")).isFalse();
  }
}
