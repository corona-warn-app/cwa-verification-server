/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, T-Systems International GmbH
 *
 * Deutsche Telekom AG, SAP AG and all other contributors /
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
package app.coronawarn.verification.services.service;

import app.coronawarn.verification.services.common.TanSourceOfTrust;
import app.coronawarn.verification.services.common.TanType;
import app.coronawarn.verification.services.domain.VerificationTan;
import app.coronawarn.verification.services.repository.VerificationTanRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * This class represents the TanService service.
 */
@Component
public class TanService {

    /**
     * The logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    @Value("${tan.valid.days}")
    private Integer TAN_VALID_IN_DAYS;
    @Value("${tan.tele.valid.hours}")
    private Integer TELE_TAN_VALID_IN_HOURS;

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

    /**
     * This Method generates a valid TAN and persists it. Returns the generated
     * TAN.
     *
     * @return
     */
    public String generateVerificationTan(String sourceOfTrust) {
        String tan = generateValidTan();
        persistTan(tan, TanType.TAN, sourceOfTrust);
        return tan;
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
    //TODO syntax constraints from Julius
    public boolean syntaxVerification(String tan) {
        return true;
    }

    //TODO syntax constraints from Julius
    /**
     * Check Tele-TAN syntax constraints.
     *
     * @param teleTan the Tele TAN
     * @return Tele TAN verification flag
     */
    private boolean syntaxTeleTanVerification(String teleTan) {
        //TODO: Needs to be implemented.
        return true;
    }

    /**
     * Verifies the tele transaction number (Tele TAN).
     *
     * @param teleTan
     * @return
     */
    public boolean verifyTeleTan(String teleTan) {
        boolean verified = false;
        if (syntaxTeleTanVerification(teleTan)) {
            Optional<VerificationTan> teleTANEntity = getEntityByTan(teleTan);
            if (teleTANEntity.isPresent() && !teleTANEntity.get().isRedeemed()) {
                verified = true;
            } else {
                LOG.warn("The Tele TAN is unknown or already redeemed.");
            }
        } else {
            LOG.warn("The Tele TAN is not valid to the syntax constraints.");
        }
        return verified;
    }

    /**
     * Returns the a Valid TAN String
     *
     * @return a Valid TAN String
     */
    public String generateValidTan() {
        boolean validTan = false;
        String newTan = "";
        while (!validTan) {
            newTan = generateTanFromUUID();
            validTan = hashTanAndCheckAvailability(newTan);
        }
        return newTan;
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
     * @param tan
     * @param tanType
     * @return
     */
    private VerificationTan persistTan(String tan, TanType tanType,
            String sourceOfTrust) {
        VerificationTan newTan = TanService.this.generateVerificationTan(tan, tanType, sourceOfTrust);
        return tanRepository.save(newTan);
    }

    /**
     * Returns the hash of the supplied string.
     *
     * @return
     */
    public String generateTeleTan() {
        //TODO clarify generation of Teletan
        return null;
    }

    private String generateTanFromUUID() {
        // A UUID is a 128 bit value
        return UUID.randomUUID().toString();
    }

    private boolean hashTanAndCheckAvailability(String tan) {
        String tanHash = hashingService.hash(tan);
        return !tanRepository.existsByTanHash(tanHash);
    }

    private VerificationTan generateVerificationTan(String tan, TanType tanType,
            String sourceOfTrust) {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime until = from.plusDays(TAN_VALID_IN_DAYS);

        VerificationTan verificationTAN = new VerificationTan();
        verificationTAN.setTanHash(hashingService.hash(tan));
        verificationTAN.setValidFrom(from);
        verificationTAN.setValidUntil(until);
        verificationTAN.setSourceOfTrust(sourceOfTrust);
        verificationTAN.setRedeemed(false);
        verificationTAN.setCreatedOn(LocalDateTime.now());
        verificationTAN.setType(tanType.name());
        return verificationTAN;
    }

    /**
     * Get existing VerificationTan by TAN from
     * {@link VerificationTanRepository}.
     *
     * @param tan the TAN
     * @return Optional VerificationTan
     */
    public Optional<VerificationTan> getEntityByTan(String tan) {
        LOG.info("TanService start getEntityByTan.");
        VerificationTan tanEntity = new VerificationTan();
        tanEntity.setTanHash(hashingService.hash(tan));
        return tanRepository.findOne(Example.of(tanEntity, ExampleMatcher.matching()));
    }

}
