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

import app.coronawarn.verification.services.common.TANType;
import app.coronawarn.verification.services.domain.VerificationTAN;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;
import app.coronawarn.verification.services.repository.VerificationTANRepository;

/**
 * This class represents the TanService service.
 *
 */
@Component
public class TanService {

    /**
     * The logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    @Value("${tan.valid.seconds}")
    Integer TAN_VALID_IN_SECONDS;
    @Value("${tan.tele.valid.seconds}")
    Integer TELE_TAN_VALID_IN_SECONDS;

    /**
     * The {@link VerficationTANRepository}.
     */
    @Autowired
    VerificationTANRepository tanRepository;

    /**
     * The {@link HashingService}.
     */
    @Autowired
    HashingService hashingService;

    /**
     * This Method generates a valid TAN and persists it.
     *
     * @return the generated TAN
     */
    public String generateVerificationTAN() {
        String tan = generateValidTan();
        persistTan(tan, TANType.TAN);
        return tan;
    }

    /**
     * This Method tries to create a valid TAN from the parameter and persists
     * it, if the TAN is valid.
     *
     * @param tan that will be hashed and persisted
     * @return the hash of the supplied string
     */
    public VerificationTAN saveTan(VerificationTAN tan) {
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
        LOG.debug("Generated new Tan %s", newTan);
        return newTan;
    }

    /**
     * Check for existing TAN in the {@link VerficationTANRepository}.
     *
     * @param tan the TAN
     * @return flag for existing TAN
     */
    public boolean checkTANAlreadyExist(String tan) {
        return hashTanAndCheckAvailability(tan);
    }

    /**
     * This method generates a {@link VerificationTAN} - entity and saves it.
     *
     * @param tan
     * @param tanType
     * @return
     */
    private VerificationTAN persistTan(String tan, TANType tanType) {
        VerificationTAN newTan = generateVerificationTAN(tan, tanType);
        return tanRepository.save(newTan);
    }

    /**
     * Returns the hash of the supplied string
     *
     * @return the hash of the supplied string
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

    private VerificationTAN generateVerificationTAN(String tan, TANType tanType) {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime until = from.plusSeconds(TAN_VALID_IN_SECONDS);

        VerificationTAN verificationTAN = new VerificationTAN();
        verificationTAN.setTanHash(hashingService.hash(tan));
        verificationTAN.setValidFrom(from);
        verificationTAN.setValidUntil(until);
        verificationTAN.setRedeemed(false);
        verificationTAN.setCreatedOn(LocalDateTime.now());
        verificationTAN.setType(tanType.name());
        return verificationTAN;
    }

    /**
     * Get existing VerificationTAN by TAN from
     * {@link VerificationTANRepository}.
     *
     * @param tan the TAN
     * @return Optional VerificationTAN
     */
    public Optional<VerificationTAN> getEntityByTAN(String tan) {
        LOG.info("VerficationTANService start getEntityByTAN.");
        VerificationTAN tanEntity = new VerificationTAN();
        tanEntity.setTanHash(hashingService.hash(tan));
        return tanRepository.findOne(Example.of(tanEntity, ExampleMatcher.matching()));
    }

}
