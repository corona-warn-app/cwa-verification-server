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
import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.model.TanType;
import app.coronawarn.verification.repository.VerificationAppSessionRepository;
import app.coronawarn.verification.repository.VerificationTanRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import rx.Single;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("local")
@SpringBootTest(
  properties = {
    "entities.cleanup.rate=1000"
  }
)
@ContextConfiguration(classes = VerificationApplication.class)
public class EntitiesCleanupServiceTest {

  public static final String TEST_GUI_HASH = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
  public static final String TEST_REG_TOK_HASH = "c775e7b757ede630cd0aa1113bd102661ab38829ca52a6422ab782862f268646";
  public static final String TEST_HASHED_TAN = "16154ea91c2c59d6ef9d0e7f902a59283b1e7ff9111570d20139a4e6b1832876";

  @Autowired
  private VerificationAppSessionRepository appSessionRepository;

  @Autowired
  private VerificationTanRepository tanRepository;

  @BeforeEach
  public void before() {
    appSessionRepository.deleteAll();
    tanRepository.deleteAll();
  }

  @Test
  public void cleanupDatabase() {
    LocalDateTime testCreationTime = LocalDateTime.now().minus(Period.ofDays(21));
    // create repo 1
    VerificationAppSession session = appSessionRepository.save(getAppSessionTestData(testCreationTime));
    Assertions.assertNotNull(session);
    Assertions.assertEquals(TEST_GUI_HASH, session.getHashedGuid());
    // create repo 2
    VerificationTan tan = tanRepository.save(getVerificationTANTestData(testCreationTime));
    Assertions.assertNotNull(tan);
    Assertions.assertEquals(TEST_HASHED_TAN, tan.getTanHash());
    // find in repos
    Optional<VerificationAppSession> findSession = appSessionRepository.findByRegistrationTokenHash(TEST_REG_TOK_HASH);
    Assertions.assertTrue(findSession.isPresent());
    Assertions.assertEquals(TEST_GUI_HASH, findSession.get().getHashedGuid());

    Assertions.assertEquals(testCreationTime.withNano(5), findSession.get().getCreatedAt().withNano(5));
    Optional<VerificationTan> findTan = tanRepository.findByTanHash(TEST_HASHED_TAN);
    Assertions.assertTrue(findTan.isPresent());
    Assertions.assertEquals(TEST_HASHED_TAN, findTan.get().getTanHash());
    Assertions.assertEquals(testCreationTime.withNano(5), findTan.get().getCreatedAt().withNano(5));
    // wait
    Single.fromCallable(() -> true).delay(1, TimeUnit.SECONDS).toBlocking().value();
    // find and check both repos clean up
    findSession = appSessionRepository.findByRegistrationTokenHash(TEST_REG_TOK_HASH);
    Assertions.assertFalse(findSession.isPresent());
    findTan = tanRepository.findByTanHash(TEST_HASHED_TAN);
    Assertions.assertFalse(findTan.isPresent());
  }

  private VerificationAppSession getAppSessionTestData(LocalDateTime testCreationTime) {
    VerificationAppSession cv = new VerificationAppSession();
    cv.setHashedGuid(TEST_GUI_HASH);
    cv.setCreatedAt(testCreationTime);
    cv.setUpdatedAt(LocalDateTime.now());
    cv.setTanCounter(0);
    cv.setSourceOfTrust(AppSessionSourceOfTrust.HASHED_GUID);
    cv.setRegistrationTokenHash(TEST_REG_TOK_HASH);
    return cv;
  }

  private VerificationTan getVerificationTANTestData(LocalDateTime testCreationTime) {
    VerificationTan cvtan = new VerificationTan();
    cvtan.setCreatedAt(testCreationTime);
    cvtan.setUpdatedAt(LocalDateTime.now());
    cvtan.setRedeemed(false);
    cvtan.setSourceOfTrust(TanSourceOfTrust.CONNECTED_LAB);
    cvtan.setTanHash(TEST_HASHED_TAN);
    cvtan.setType(TanType.TAN);
    cvtan.setValidFrom(LocalDateTime.now().minusDays(5));
    cvtan.setValidUntil(LocalDateTime.now().plusDays(7));
    return cvtan;
  }
}
