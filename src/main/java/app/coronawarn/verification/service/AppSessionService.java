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

import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.TeleTanType;
import app.coronawarn.verification.repository.VerificationAppSessionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * This class represents the VerificationAppSession service.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AppSessionService {

  private static final Integer TOKEN_PADDING_LENGTH = 1;
  /**
   * The {@link VerificationAppSessionRepository}.
   */
  @NonNull
  private final VerificationAppSessionRepository appSessionRepository;

  /**
   * The {@link HashingService}.
   */
  @NonNull
  private final HashingService hashingService;

  /**
   * Creates an AppSession-Entity.
   *
   * @param registrationToken Token for registration
   * @return appSession for registrationToken
   */
  public VerificationAppSession generateAppSession(String registrationToken) {
    log.info("Create the app session entity with the created registration token.");
    VerificationAppSession appSession = new VerificationAppSession();
    appSession.setCreatedAt(LocalDateTime.now());
    appSession.setUpdatedAt(LocalDateTime.now());
    appSession.setRegistrationTokenHash(hashingService.hash(registrationToken));
    return appSession;
  }

  private String generateRegistrationToken() {
    return UUID.randomUUID().toString();
  }

  /**
   * This method generates a registration Token by a guid .
   *
   * @param hashedGuid the hashed guid
   * @return an {@link ResponseEntity}
   */
  public ResponseEntity<RegistrationToken> generateRegistrationTokenByGuid(
    String hashedGuid, String hashedGuidDob, String fake) {

    if (checkRegistrationTokenAlreadyExistsForGuid(hashedGuid)) {
      log.warn("The registration token already exists for the hashed guid.");
      return ResponseEntity.badRequest().build();
    }

    if (hashedGuidDob != null && checkRegistrationTokenAlreadyExistsForGuid(hashedGuidDob)) {
      log.warn("The registration token already exists for the hashed guid dob.");
      return ResponseEntity.badRequest().build();
    }

    log.info("Start generating a new registration token for the given hashed guid.");

    String registrationToken = generateRegistrationToken();
    VerificationAppSession appSession = generateAppSession(registrationToken);
    appSession.setHashedGuid(hashedGuid);
    appSession.setHashedGuidDob(hashedGuidDob);
    appSession.setSourceOfTrust(AppSessionSourceOfTrust.HASHED_GUID);
    saveAppSession(appSession);

    log.info("Returning the successfully created registration token.");
    return ResponseEntity.status(HttpStatus.CREATED).body(
      getBackwardCompatibleRegistrationToken(registrationToken, fake));

  }

  /**
   * This method generates a registration Token by a TeleTAN.
   *
   * @param teleTan the TeleTan
   * @return an {@link ResponseEntity}
   */
  public ResponseEntity<RegistrationToken> generateRegistrationTokenByTeleTan(
    String teleTan, String fake, TeleTanType teleTanType) {
    if (checkRegistrationTokenAlreadyExistForTeleTan(teleTan)) {
      log.warn("The registration token already exists for this TeleTAN.");
      return ResponseEntity.badRequest().build();
    } else {
      log.info("Start generating a new registration token for the given TeleTAN.");
      String registrationToken = generateRegistrationToken();
      VerificationAppSession appSession = generateAppSession(registrationToken);
      appSession.setTeleTanHash(hashingService.hash(teleTan));
      appSession.setSourceOfTrust(AppSessionSourceOfTrust.TELETAN);
      appSession.setTeleTanType(teleTanType);
      saveAppSession(appSession);
      log.info("Returning the successfully created registration token.");
      return ResponseEntity.status(HttpStatus.CREATED).body(
        getBackwardCompatibleRegistrationToken(registrationToken, fake));
    }
  }

  /**
   * Persists the specified entity of {@link VerificationAppSession} instances.
   *
   * @param appSession the verification app session entity
   */
  public void saveAppSession(VerificationAppSession appSession) {
    log.info("Start saveAppSession.");
    appSessionRepository.save(appSession);
  }

  /**
   * Get existing VerificationAppSession for Reg Token from {@link VerificationAppSessionRepository}.
   *
   * @param registrationToken the registrationToken
   * @return Optional VerificationAppSession
   */
  public Optional<VerificationAppSession> getAppSessionByToken(String registrationToken) {
    log.info("Start getAppSessionByToken.");
    return appSessionRepository.findByRegistrationTokenHash(hashingService.hash(registrationToken));
  }

  /**
   * Check for existing hashed GUID Token in the {@link VerificationAppSessionRepository}.
   *
   * @param hashedGuid the hashed guid
   * @return flag for existing guid
   */
  public boolean checkRegistrationTokenAlreadyExistsForGuid(String hashedGuid) {
    log.info("Start checkRegistrationTokenAlreadyExistsForGuid.");
    return appSessionRepository.findByHashedGuidOrHashedGuidDob(hashedGuid, hashedGuid).isPresent();
  }

  /**
   * Check for existing hashed TeleTAN in the {@link VerificationAppSessionRepository}.
   *
   * @param teleTan the teleTAN
   * @return flag for existing teleTAN
   */
  public boolean checkRegistrationTokenAlreadyExistForTeleTan(String teleTan) {
    log.info("Start checkTeleTanAlreadyExistForTeleTan.");
    return appSessionRepository.findByTeleTanHash(hashingService.hash(teleTan)).isPresent();
  }

  private RegistrationToken getBackwardCompatibleRegistrationToken(String registrationToken, String fake) {
    if (fake == null) {
      return new RegistrationToken(registrationToken);
    }
    return new RegistrationToken(registrationToken, RandomStringUtils.randomAlphanumeric(TOKEN_PADDING_LENGTH));
  }
}
