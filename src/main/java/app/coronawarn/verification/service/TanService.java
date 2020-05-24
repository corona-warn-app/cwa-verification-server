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

import app.coronawarn.verification.domain.VerificationTan;
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
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;


/**
 * This class represents the TanService service.
 */
@Slf4j
@Component
public class TanService {

  private static final Integer TELE_TAN_LENGTH = 7;
  private static final String TAN_TAN_PATTERN = "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}";

  // Exclude characters which can be confusing in some fonts like 0-O or i-I-l.
  private static final String TELE_TAN_ALLOWED_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
  private static final String TELE_TAN_PATTERN = "^[" + TELE_TAN_ALLOWED_CHARS + "]{" + TELE_TAN_LENGTH + "}$";
  private static final Pattern PATTERN = Pattern.compile(TELE_TAN_PATTERN);
  private static final Pattern TAN_PATTERN = Pattern.compile(TAN_TAN_PATTERN);

  @Value("${tan.valid.days}")
  private Integer tanValidInDays;
  @Value("${tan.tele.valid.hours}")
  private Integer teleTanValidInHours;

  /**
   * The {@link VerificationTanRepository}.
   */
  @Autowired
  private VerificationTanRepository tanRepository;

  /**
   * The {@link HashingService}.
   */
  @Autowired
  private HashingService hashingService;

  /*
   * The random number generator used by this class to create random
   * based UUIDs. In a holder class to defer initialization until needed.
   */
  private static class Holder {
    static final SecureRandom numberGenerator = new SecureRandom();
  }

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
   * Check TAN syntax constraints.
   *
   * @param tan the TAN
   * @return TAN verification flag
   */
  public boolean syntaxVerification(String tan) {
    Matcher matcher = TAN_PATTERN.matcher(tan);
    return matcher.find();
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
      log.info(teleTanEntity.toString());
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
   * Returns the a Valid TAN String.
   *
   * @return a Valid TAN String
   */
  public String generateValidTan() {
    return IntStream.range(0, TELE_TAN_LENGTH)
      .mapToObj(i -> TELE_TAN_ALLOWED_CHARS.charAt(Holder.numberGenerator.nextInt(TELE_TAN_ALLOWED_CHARS.length())))
      .collect(Collector.of(
        StringBuilder::new,
        StringBuilder::append,
        StringBuilder::append,
        StringBuilder::toString));
  }

  /**
   * Check for existing TAN in the {@link VerificationTanRepository}.
   *
   * @param tan the TAN
   * @return flag for existing TAN
   */
  public boolean checkTanAlreadyExist(String tan) {
    return hashTanAndCheckAvailability(tan);
  }

  /**
   * This method generates a {@link VerificationTan} - entity and saves it.
   *
   * @param tan     the TAN
   * @param tanType the TAN type
   * @return the persisted TAN
   */
  private VerificationTan persistTan(String tan, TanType tanType, String sourceOfTrust) {
    VerificationTan newTan = generateVerificationTan(tan, tanType, sourceOfTrust);
    return tanRepository.save(newTan);
  }

  /**
   * Returns the a new valid TeleTan String.
   *
   * @return a new TeleTan
   */
  public String generateTeleTan() {
    /*
     * The generation of a TeleTan is a temporary solution and may be subject to later changes.
     */
    String generatedTeleTan = "";
    boolean isTeleTanValid = false;

    while (!isTeleTanValid) {
      generatedTeleTan = RandomString.make(TELE_TAN_LENGTH);
      isTeleTanValid = isTeleTanValid(generatedTeleTan);
    }
    return generatedTeleTan;
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

  private String generateTanFromUuid() {
    // A UUID is a 128 bit value
    return UUID.randomUUID().toString();
  }

  private boolean hashTanAndCheckAvailability(String tan) {
    String tanHash = hashingService.hash(tan);
    return !tanRepository.existsByTanHash(tanHash);
  }

  /**
   * This Method generates a valid TAN and persists it. Returns the generated TAN.
   *
   * @param sourceOfTrust sets the source of Trust for the Tan
   * @return Tan a valid tan with given source of Trust
   */
  public String generateVerificationTan(String sourceOfTrust) {
    String tan = generateValidTan();
    persistTan(tan, TanType.TAN, sourceOfTrust);
    return tan;
  }

  protected VerificationTan generateVerificationTan(String tan, TanType tanType, String sourceOfTrust) {
    LocalDateTime from = LocalDateTime.now();
    LocalDateTime until;

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
   * Get existing VerificationTan by TAN from
   * {@link VerificationTanRepository}.
   *
   * @param tan the TAN
   * @return Optional VerificationTan
   */
  public Optional<VerificationTan> getEntityByTan(String tan) {
    log.info("TanService start getEntityByTan.");
    VerificationTan tanEntity = new VerificationTan();
    tanEntity.setTanHash(hashingService.hash(tan));
    return tanRepository.findOne(Example.of(tanEntity, ExampleMatcher.matching()));
  }
}
