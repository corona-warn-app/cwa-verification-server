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
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.model.TanType;
import app.coronawarn.verification.repository.VerificationTanRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VerificationApplication.class)
public class TanServiceTest {

  public static final String TEST_TAN = "1ea6ce8a-9740-11ea-bb37-0242ac130002";
  public static final String TEST_TAN_HASH = "8de76b627f0be70ea73c367a9a560d6a987eacec71f57ca3d86b2e4ed5b6f780";
  public static final String TEST_GUI_HASH = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
  public static final String TEST_TAN_TYPE = TanType.TAN.name();
  public static final String TEST_TELE_TAN = "R3ZNUEV";
  public static final String TEST_TELE_TAN_HASH = "a865dd70e90e02286ea06a25f0babe88020d27d2923241ad792fac81f1254c75";
  // note the length of teleTAN, is made up of the teletan length and the check digit
  private static final String TELE_TAN_REGEX = "^[2-9A-HJ-KMNP-Z]{10}$";
  private static final String TAN_REGEX = "^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$";
  private static final TanSourceOfTrust TEST_TELE_TAN_SOURCE_OF_TRUST = TanSourceOfTrust.TELETAN;
  private static final TanSourceOfTrust TEST_TAN_SOURCE_OF_TRUST = TanSourceOfTrust.CONNECTED_LAB;
  private static final Pattern TELE_TAN_PATTERN = Pattern.compile(TELE_TAN_REGEX);
  private static final Pattern TAN_PATTERN = Pattern.compile(TAN_REGEX);
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS");
  private static final LocalDateTime TAN_VALID_UNTIL_IN_DAYS = LocalDateTime.now().plusDays(14);
  private static final LocalDateTime TELE_TAN_VALID_UNTIL_IN_HOURS = LocalDateTime.now().plusHours(1);

  @Autowired
  private TanService tanService;

  @Autowired
  private VerificationTanRepository tanRepository;

  @Before
  public void setUp() {
    tanRepository.deleteAll();
  }

  /**
   * Test delete Tan.
   */
  @Test
  public void deleteTan() {
    VerificationTan tan = new VerificationTan();
    LocalDateTime start = LocalDateTime.parse(LocalDateTime.now().format(FORMATTER));
    tan.setCreatedAt(start);
    tan.setUpdatedAt(start);
    tan.setRedeemed(false);
    tan.setTanHash(TEST_TAN_HASH);

    tan.setValidFrom(start);
    tan.setValidUntil(LocalDateTime.parse((TAN_VALID_UNTIL_IN_DAYS.format(FORMATTER))));
    tan.setType(TEST_TAN_TYPE);
    tan.setSourceOfTrust(TEST_TELE_TAN_SOURCE_OF_TRUST);
    tanService.saveTan(tan);

    Optional<VerificationTan> tanFromDB = tanService.getEntityByTan(TEST_TAN);
    Assert.assertEquals(tan, tanFromDB.orElse(null));
    tanService.deleteTan(tan);
    tanFromDB = tanService.getEntityByTan(TEST_TAN);
    assertFalse(tanFromDB.isPresent());
  }

  /**
   * Test saveTan.
   */
  @Test
  public void saveTanTest() {
    VerificationTan tan = new VerificationTan();
    tan.setCreatedAt(LocalDateTime.now());
    tan.setUpdatedAt(LocalDateTime.now());
    tan.setRedeemed(false);
    tan.setTanHash(TEST_GUI_HASH);
    tan.setValidFrom(LocalDateTime.now());
    tan.setValidUntil(TAN_VALID_UNTIL_IN_DAYS);
    tan.setType(TEST_TAN_TYPE);
    tan.setSourceOfTrust(TEST_TELE_TAN_SOURCE_OF_TRUST);
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
    tan.setSourceOfTrust(TEST_TELE_TAN_SOURCE_OF_TRUST);
    tanService.saveTan(tan);

    Optional<VerificationTan> tanFromDB = tanService.getEntityByTan(TEST_TAN);
    Assert.assertEquals(tan, tanFromDB.orElse(null));
  }

  @Test
  public void checkTanAlreadyExist() {
    VerificationTan tan = new VerificationTan();
    LocalDateTime start = LocalDateTime.parse(LocalDateTime.now().format(FORMATTER));
    tan.setCreatedAt(start);
    tan.setUpdatedAt(start);
    tan.setRedeemed(false);
    tan.setTanHash(TEST_TELE_TAN_HASH);
    tan.setValidFrom(start);
    tan.setValidUntil(LocalDateTime.parse((TELE_TAN_VALID_UNTIL_IN_HOURS.format(FORMATTER))));
    tan.setType(TanType.TELETAN.name());
    tan.setSourceOfTrust(TEST_TELE_TAN_SOURCE_OF_TRUST);
    tanService.saveTan(tan);
    assertFalse(tanService.checkTanNotExist(TEST_TELE_TAN));
  }

  @Test
  public void generateVerificationTan() {
    String tan = tanService.generateVerificationTan(TEST_TELE_TAN_SOURCE_OF_TRUST);
    assertTrue(syntaxTanVerification(tan));
    assertFalse(tan.isEmpty());
  }

  @Test
  public void generateValidTan() {
    String tan = tanService.generateVerificationTan(TEST_TAN_SOURCE_OF_TRUST);
    assertTrue(syntaxTanVerification(tan));
    assertFalse(tan.isEmpty());
  }

  @Test
  public void generateTeleTan() {
    String teleTan = tanService.generateTeleTan();
    Matcher matcher = TELE_TAN_PATTERN.matcher(teleTan);
    assertTrue(matcher.find());
  }

  @Test
  public void verifyTeletan() {
    String teleTan = tanService.generateVerificationTeleTan();
    assertTrue(tanService.checkTanNotExist(TEST_TELE_TAN));
    assertTrue(tanService.verifyTeleTan(teleTan));
    assertFalse(tanService.verifyTeleTan("R3ZNUI0"));
  }

  @Test
  public void verifyAlreadyRedeemedTeleTan() {
    String teleTan = tanService.generateVerificationTeleTan();
    VerificationTan teleTanFromDB = tanService.getEntityByTan(teleTan).orElse(null);
    if (teleTanFromDB != null) {
      teleTanFromDB.setRedeemed(true);
    }
    tanService.saveTan(teleTanFromDB);
    assertFalse(tanService.verifyTeleTan(teleTan));
  }

  @Test
  public void verifyUnknownTeleTan() {
    String teleTan = tanService.generateTeleTan();
    assertFalse(tanService.verifyTeleTan(teleTan));
  }

  @Test
  public void verifyExpiredTeleTan() {
    String teleTan = tanService.generateVerificationTeleTan();
    VerificationTan teleTanFromDB = tanService.getEntityByTan(teleTan).orElse(null);
    LocalDateTime validFrom = LocalDateTime.now().minusHours(1).minusMinutes(1);
    if (teleTanFromDB != null) {
      teleTanFromDB.setValidFrom(validFrom);
      teleTanFromDB.setValidUntil(validFrom.plusHours(1));
    }
    tanService.saveTan(teleTanFromDB);
    assertFalse(tanService.verifyTeleTan(teleTan));
  }

  @Test
  public void testTeleTANFormat() {
    assertThat(tanService.isTeleTanValid("29ABCZAE4E")).isTrue();
    assertThat(tanService.isTeleTanValid("29ABCzAE4O")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAABCE40")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAABCE41")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAABCE4I")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAABCE4L")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAABCEil")).isFalse();
    assertThat(tanService.isTeleTanValid("29zABCA?ßö")).isFalse();
    assertThat(tanService.isTeleTanValid("29zABCAE4EZ")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAABCE4")).isFalse();
    assertThat(tanService.isTeleTanValid("29zAABCL4-")).isFalse();
  }

  /**
   * Check Tele-TAN syntax constraints.
   *
   * @param tan the Tele TAN
   * @return Tele TAN verification flag
   */
  private boolean syntaxTanVerification(String tan) {
    Matcher matcher = TAN_PATTERN.matcher(tan);
    return matcher.find();
  }
}
