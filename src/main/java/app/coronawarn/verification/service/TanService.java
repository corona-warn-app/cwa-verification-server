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
import app.coronawarn.verification.model.TeleTanType;
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
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class represents the TanService service.
 */
@Slf4j
@Component
public class TanService {

 
  private final VerificationApplicationConfig verificationApplicationConfig;

  /**
   * The {@link VerificationTanRepository}.
   */
  private final VerificationTanRepository tanRepository;
  /**
   * The {@link HashingService}.
   */
  private final HashingService hashingService;

  private final Pattern teleTanPattern;
  private final int teleTanLength;

  /**
   * Constructor for the TanService that also builds the pattern for tele tan verification.
   *
   * @param verificationApplicationConfig the {@link VerificationApplicationConfig} with needed tan configurations
   * @param tanRepository the {@link VerificationTanRepository} where tans are queried and inserted
   * @param hashingService the {@link HashingService} implementation
   */
  public TanService(
    @NonNull VerificationApplicationConfig verificationApplicationConfig,
    @NonNull VerificationTanRepository tanRepository,
    @NonNull HashingService hashingService
  ) {
    this.verificationApplicationConfig = verificationApplicationConfig;
    this.tanRepository = tanRepository;
    this.hashingService = hashingService;
    this.teleTanLength = verificationApplicationConfig.getTan().getTele().getValid().getLength();
    this.teleTanPattern = Pattern.compile("^["
      + verificationApplicationConfig.getTan().getTele().getValid().getChars()
      + "]{"
      + (verificationApplicationConfig.getTan().getTele().getValid().getLength() + 1)
      + "}$");
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
    Matcher matcher = teleTanPattern.matcher(teleTan);
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
   * @param tan     the TAN
   * @param tanType the TAN type
   * @param teleTanType type of the teleTan
   * @return the persisted TAN
   */
  private VerificationTan persistTan(
    String tan, TanType tanType, TanSourceOfTrust sourceOfTrust, TeleTanType teleTanType) {
    VerificationTan newTan = generateVerificationTan(tan, tanType, sourceOfTrust, teleTanType);
    return tanRepository.save(newTan);
  }

  /**
   * Creates a new TeleTan String.
   *
   * @return a new teleTAN
   */
  protected String createTeleTan() {
    final int length = verificationApplicationConfig.getTan().getTele().getValid().getLength();
    final String chars = verificationApplicationConfig.getTan().getTele().getValid().getChars();
    String teletan = IntStream.range(0, length)
      .mapToObj(i -> chars.charAt(Holder.NUMBER_GENERATOR.nextInt(chars.length())))
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
   * Returns a generated valid teleTAN and persists it.
   *
   * @return a valid teleTAN
   */
  public String generateVerificationTeleTan(@NotNull TeleTanType teleTanType) {
    String teleTan = generateValidTan(this::createTeleTan);
    persistTan(teleTan, TanType.TELETAN, TanSourceOfTrust.TELETAN, teleTanType);
    return teleTan;
  }

  /**
   * This Method generates a valid TAN and persists it. Returns the generated TAN.
   *
   * @param sourceOfTrust sets the source of Trust for the Tan
   * @param teleTanType type of the teleTan
   * @return a valid tan with given source of Trust
   */
  public String generateVerificationTan(TanSourceOfTrust sourceOfTrust, TeleTanType teleTanType) {
    String tan = generateValidTan(this::createTanFromUuid);
    persistTan(tan, TanType.TAN, sourceOfTrust, teleTanType);
    return tan;
  }

  /**
   * This method generates a valid TAN Object but doesn't persist it.
   * @param tan alphanumeric tan
   * @param tanType type of the tan
   * @param sourceOfTrust source of trust of the tan
   * @return Tan object
   */
  public VerificationTan generateVerificationTan(
    String tan, TanType tanType, TanSourceOfTrust sourceOfTrust, TeleTanType teleTanType) {

    LocalDateTime from = LocalDateTime.now();
    LocalDateTime until;
    int tanValidInDays = verificationApplicationConfig.getTan().getValid().getDays();
    int teleTanValidInHours = verificationApplicationConfig.getTan().getTele().getValid().getHours();
    int eventTeleTanValidInDays = verificationApplicationConfig.getTan().getTele().getValid().getEventDays();

    if (tanType == TanType.TELETAN) {
      if (teleTanType == TeleTanType.TEST) {
        until = from.plusHours(teleTanValidInHours);
      } else {
        until = from.plusDays(eventTeleTanValidInDays);
      }
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
    verificationTan.setType(tanType);
    verificationTan.setTeleTanType(teleTanType);
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

  /**
   * Checks whether the rate limit for new TeleTans is not exceeded.
   *
   * @return true if new TeleTans can be created false if not.
   */
  public boolean isTeleTanRateLimitNotExceeded() {
    int maxNumberOfTans = verificationApplicationConfig.getTan().getTele().getRateLimiting().getCount();
    int thresholdInPercent = verificationApplicationConfig.getTan().getTele().getRateLimiting().getThresholdInPercent();
    int thresholdTans = thresholdInPercent * maxNumberOfTans / 100;
    int timeWindow = verificationApplicationConfig.getTan().getTele().getRateLimiting().getSeconds();

    LocalDateTime timestamp = LocalDateTime.now().minusSeconds(timeWindow);
    int countedTans = tanRepository.countByCreatedAtIsAfterAndTypeIs(timestamp, TanType.TELETAN);

    boolean result = countedTans < maxNumberOfTans;

    if (!result) {
      log.warn("The TeleTan rate limit is exceeded! (maximum {} tans within {} seconds)", maxNumberOfTans, timeWindow);
    } else if (countedTans >= thresholdTans) {
      log.warn("The TeleTan rate limit threshold of {}% is reached!"
        + " (maximum {} tans within {} seconds)", thresholdInPercent, maxNumberOfTans, timeWindow);
    }

    return result;
  }

  /*
   * The random number generator used by this class to create random
   * based UUIDs. In a holder class to defer initialization until needed.
   */
  private static class Holder {

    static final SecureRandom NUMBER_GENERATOR = new SecureRandom();
  }
}
