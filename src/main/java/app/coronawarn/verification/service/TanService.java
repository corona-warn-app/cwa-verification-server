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
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class represents the TanService service.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TanService {

  // Tele-TANs are a shorter, easier to communicate form of TAN
  private static final int TELE_TAN_LENGTH = 7;
  // Exclude characters which can be confusing in some fonts like 0-O or i-I-l.
  private static final String TELE_TAN_ALLOWED_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
  private static final String TELE_TAN_PATTERN = "^[" + TELE_TAN_ALLOWED_CHARS + "]{" + TELE_TAN_LENGTH + "}$";
  private static final Pattern PATTERN = Pattern.compile(TELE_TAN_PATTERN);

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

  @NonNull
  private final VerificationApplicationConfig verificationApplicationConfig;

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
   * Check Tele-TAN syntax constraints.
   *
   * @param teleTan the Tele TAN
   * @return Tele TAN verification flag
   */
  private boolean syntaxTeleTanVerification(String teleTan) {
    Matcher matcher = PATTERN.matcher(teleTan);
    return matcher.find();
  }

  /**
   * Verifies the tele transaction number (Tele TAN).
   *
   * @param teleTan the Tele TAN to verify
   * @return verified is teletan is verified
   */
  public boolean verifyTeleTan(String teleTan) {
    boolean verified = false;
    if (syntaxTeleTanVerification(teleTan)) {
      Optional<VerificationTan> teleTanEntity = getEntityByTan(teleTan);
      if (teleTanEntity.isPresent() && !teleTanEntity.get().isRedeemed()) {
        verified = true;
      } else {
        log.warn("The Tele TAN is unknown or already redeemed.");
      }
    } else {
      log.warn("The Tele TAN is not valid to the syntax constraints.");
    }
    return verified;
  }

  /**
   * Generates a new, valid TAN String.
   * A TAN is considered as valid if it is not yet stored in the database.
   *
   * @param tanCreator a supplier which creates a new TAN
   * @return a Valid TAN String
   */
  protected String generateValidTan(Supplier<String> tanCreator) {
    boolean validTan = false;
    String newTan = "";
    while (!validTan) {
      newTan = tanCreator.get();
      validTan = checkTanNotExist(newTan);
    }
    return newTan;
  }

  /**
   * This method generates a {@link VerificationTan} - entity and saves it.
   *
   * @param tan the TAN
   * @param tanType the TAN type
   * @return the persisted TAN
   */
  private VerificationTan persistTan(String tan, TanType tanType, TanSourceOfTrust sourceOfTrust) {
    VerificationTan newTan = generateVerificationTan(tan, tanType, sourceOfTrust);
    return tanRepository.save(newTan);
  }

  /**
   * Creates a new TeleTan String.
   *
   * @return a new TeleTan
   */
  protected String createTeleTan() {
    return IntStream.range(0, TELE_TAN_LENGTH)
        .mapToObj(i -> TELE_TAN_ALLOWED_CHARS.charAt(Holder.NUMBER_GENERATOR.nextInt(TELE_TAN_ALLOWED_CHARS.length())))
        .collect(Collector.of(
            StringBuilder::new,
            StringBuilder::append,
            StringBuilder::append,
            StringBuilder::toString));
  }

  /**
   * Returns the if a Tele Tan matches the Pattern requirements.
   *
   * @param teleTan the Tele TAN to check
   * @return The validity of the Tele TAN
   */
  public boolean isTeleTanValid(String teleTan) {
    return syntaxTeleTanVerification(teleTan);
  }

  /**
   * Created a new TAN String.
   *
   * @return a new TAN
   */
  protected String createTanFromUuid() {
    // A UUID is a 128 bit value
    return UUID.randomUUID().toString();
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
   * Returns a generated valid tele TAN and persists it.
   *
   * @return a valid tele TAN
   */
  public String generateVerificationTeleTan() {
    String teleTan = generateValidTan(this::createTeleTan);
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
    String tan = generateValidTan(this::createTanFromUuid);
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
    log.info("TanService start getEntityByTan.");
    return tanRepository.findByTanHash(hashingService.hash(tan));
  }

  /*
   * The random number generator used by this class to create random
   * based UUIDs. In a holder class to defer initialization until needed.
   */
  private static class Holder {

    static final SecureRandom NUMBER_GENERATOR = new SecureRandom();
  }
}
