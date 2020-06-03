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

import app.coronawarn.verification.config.VerificationApplicationConfig;
import app.coronawarn.verification.repository.VerificationAppSessionRepository;
import app.coronawarn.verification.repository.VerificationTanRepository;
import java.time.LocalDateTime;
import java.time.Period;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * A Service to delete entities that are older than configured days.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EntitiesCleanupService {

  private final VerificationApplicationConfig applicationConfig;
  private final VerificationAppSessionRepository appSessionRepository;
  private final VerificationTanRepository tanRepository;

  /**
   * All entities that are older than configured days get deleted.
   */
  @Scheduled(
    fixedDelayString = "${entities.cleanup.rate}"
  )
  @Transactional
  public void cleanup() {
    log.info("cleanup execution");
    appSessionRepository.deleteByCreatedAtBefore(LocalDateTime.now()
      .minus(Period.ofDays(applicationConfig.getEntities().getCleanup().getDays())));
    tanRepository.deleteByCreatedAtBefore(LocalDateTime.now()
      .minus(Period.ofDays(applicationConfig.getEntities().getCleanup().getDays())));
  }
}
