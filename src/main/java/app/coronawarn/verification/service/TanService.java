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
import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.model.TanType;
import app.coronawarn.verification.repository.VerificationTanRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class represents the TanService service.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TanService {

  // TANs are UUIDs
  private static final String UUID_PATTERN = "^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$";
  private static final String TAN_TAN_PATTERN = UUID_PATTERN;
  private static final Pattern TAN_PATTERN = Pattern.compile(TAN_TAN_PATTERN);

  // Tele-TANs are a shorter, easier to communicate form of TAN
  @Value("${tan.tele.valid.length}")
  private static int TELE_TAN_LENGTH = 9;
  private static final int CHECK_DIGIT_LENGTH = 1;
  
  // Exclude characters which can be confusing in some fonts like 0-O or i-I-l.
  private static final String TELE_TAN_ALLOWED_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
  // Note the total length of teleTAN, is made up of the teletan length and the check digit
  private static final int TELE_TAN_TOTAL_LENGTH = TELE_TAN_LENGTH + CHECK_DIGIT_LENGTH;
  private static final String TELE_TAN_PATTERN = "^[" + TELE_TAN_ALLOWED_CHARS + "]{" + TELE_TAN_TOTAL_LENGTH + "}$";
  private static final Pattern PATTERN = Pattern.compile(TELE_TAN_PATTERN);

  @NonNull
  private final VerificationApplicationConfig verificationApplicationConfig;

  /**
   * The {@link VerificationTanRepository}.
   */
  @NonNull
  private final VerificationTanRepository tanRepository;
  /**
   * The {@link HashingService}.
   */
  @NonNull
  private final HashingService hashingService;

  /**
   * Saves a {@link VerificationTan} into the database.
   *
   * @param tan {@link VerificationTan}
   * @return {@link VerificationTan}
   */
  public VerificationTan saveTan(VerificationTan tan) {
    return tanRepository.save(tan);
  }

  /**
   * Deletes a {@link VerificationTan} from the database.
   *
   * @param tan the tan which will be deleted
   */
  public void deleteTan(VerificationTan tan) {
    tanRepository.delete(tan);
  }

  /**
   * Check teleTAN syntax constraints.
   *
   * @param teleTan the teleTAN
   * @return teleTAN verification flag
   */
  private boolean syntaxTeleTanVerification(String teleTan) {
    Matcher matcher = PATTERN.matcher(teleTan);
    return matcher.find();
  }

  /**
   * Verifies the tele transaction number (teleTAN).
   *
   * @param teleTan the teleTAN to verify
   * @return is teleTAN verified
   */
  public boolean verifyTeleTan(String teleTan) {
    boolean verified = false;
    if (syntaxTeleTanVerification(teleTan)) {
      Optional<VerificationTan> teleTanEntity = getEntityByTan(teleTan);
      if (teleTanEntity.isPresent() && teleTanEntity.get().canBeRedeemed(LocalDateTime.now())) {
        verified = true;
      } else {
        log.warn("The teleTAN is unknown, expired or already redeemed.");
      }
    } else {
      log.warn("The teleTAN is not valid to the syntax constraints.");
    }
    return verified;
  }

  /**
   * Returns the a Valid TAN String.
   *
   * @return a Valid TAN String
   */
  private String generateValidTan() {
    boolean validTan = false;
    String newTan = "";
    while (!validTan) {
      newTan = UUID.randomUUID().toString();
      validTan = checkTanNotExist(newTan);
    }
    return newTan;
  }

  /**
   * This method generates a {@link VerificationTan} - entity and saves it.
   *
   * @param tan     the TAN
   * @param tanType the TAN type
   * @return the persisted TAN
   */
  private VerificationTan persistTan(String tan, TanType tanType, TanSourceOfTrust sourceOfTrust) {
    VerificationTan newTan = generateVerificationTan(tan, tanType, sourceOfTrust);
    return tanRepository.save(newTan);
  }

  /**
   * Returns the a new valid teleTAN String.
   *
   * @return a new teleTAN
   */
  public String generateTeleTan() {
    String teletan = IntStream.range(0, TELE_TAN_LENGTH)
      .mapToObj(i -> TELE_TAN_ALLOWED_CHARS.charAt(Holder.NUMBER_GENERATOR.nextInt(TELE_TAN_ALLOWED_CHARS.length())))
      .collect(Collector.of(
        StringBuilder::new,
        StringBuilder::append,
        StringBuilder::append,
        StringBuilder::toString));
    return teletan + hashingService.getCheckDigit(teletan);
  }

  /**
   * Returns the if a teleTAN matches the Pattern requirements.
   *
   * @param teleTan the teleTAN to check
   * @return The validity of the teleTAN
   */
  public boolean isTeleTanValid(String teleTan) {
    return syntaxTeleTanVerification(teleTan);
  }

  /**
   * Check for existing TAN in the {@link VerificationTanRepository}.
   *
   * @param tan the TAN
   * @return flag for existing TAN
   */
  public boolean checkTanNotExist(String tan) {
    String tanHash = hashingService.hash(tan);
    return !tanRepository.existsByTanHash(tanHash);
  }

  /**
   * Returns a generated valid teleTAN and persists it.
   *
   * @return a valid teleTAN
   */
  public String generateVerificationTeleTan() {
    String teleTan = generateTeleTan();
    persistTan(teleTan, TanType.TELETAN, TanSourceOfTrust.TELETAN);
    return teleTan;
  }

  /**
   * This Method generates a valid TAN and persists it. Returns the generated TAN.
   *
   * @param sourceOfTrust sets the source of Trust for the Tan
   * @return a valid tan with given source of Trust
   */
  public String generateVerificationTan(TanSourceOfTrust sourceOfTrust) {
    String tan = generateValidTan();
    persistTan(tan, TanType.TAN, sourceOfTrust);
    return tan;
  }

  protected VerificationTan generateVerificationTan(String tan, TanType tanType, TanSourceOfTrust sourceOfTrust) {
    LocalDateTime from = LocalDateTime.now();
    LocalDateTime until;
    int tanValidInDays = verificationApplicationConfig.getTan().getValid().getDays();
    int teleTanValidInHours = verificationApplicationConfig.getTan().getTele().getValid().getHours();
    if (tanType == TanType.TELETAN) {
      until = from.plusHours(teleTanValidInHours);
    } else {
      until = from.plusDays(tanValidInDays);
    }

    VerificationTan verificationTan = new VerificationTan();
    verificationTan.setTanHash(hashingService.hash(tan));
    verificationTan.setValidFrom(from);
    verificationTan.setValidUntil(until);
    verificationTan.setSourceOfTrust(sourceOfTrust);
    verificationTan.setRedeemed(false);
    verificationTan.setCreatedAt(LocalDateTime.now());
    verificationTan.setUpdatedAt(LocalDateTime.now());
    verificationTan.setType(tanType.name());
    return verificationTan;
  }

  /**
   * Get existing VerificationTan by TAN from {@link VerificationTanRepository}.
   *
   * @param tan the TAN
   * @return Optional VerificationTan
   */
  public Optional<VerificationTan> getEntityByTan(String tan) {
    log.info("Start getEntityByTan.");
    return tanRepository.findByTanHash(hashingService.hash(tan));
  }

  @Value("${tan.tele.valid.length}")
  private void setTeleTanLength(String length) {
    TanService.TELE_TAN_LENGTH = Integer.valueOf(length);
  }

  /*
   * The random number generator used by this class to create random
   * based UUIDs. In a holder class to defer initialization until needed.
   */
  private static class Holder {

    static final SecureRandom NUMBER_GENERATOR = new SecureRandom();
  }
}
