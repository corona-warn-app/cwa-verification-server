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

import app.coronawarn.verification.services.domain.CoronaVerificationTAN;
import app.coronawarn.verification.services.repository.CoronaVerficationTANRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class represents the TanService service.
 *
 */
@Component
public class TanService
{

    private static final Logger LOG = LogManager.getLogger();

    @Value("${tan.valid.seconds}")
    Integer TAN_VALID_IN_SECONDS;
    @Value("${tan.tele.valid.seconds}")
    Integer TELE_TAN_VALID_IN_SECONDS;

    @Autowired
    CoronaVerficationTANRepository tanRepository;

    @Autowired
    HashingService hashingService;

    /**
     * This Method generates a valid Tan and persists it
     *
     * @return CoronaVerificationTAN of the generated Tan
     */
    public CoronaVerificationTAN generateCoronaVerificationTAN() {
        String tan = generateValidTan();
        return persistTan(tan);
    }

    /**
     * This Method tries to create a valid Tan from the parameter and persists
     * it if it is valid
     *
     * @param tan that will be hashed and persisted
     * @return the hash of the supplied string
     */
    public CoronaVerificationTAN saveTan(CoronaVerificationTAN tan) {
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
     * Check TAN Expiration.
     *
     * @param tan the TAN
     * @return TAN expiration flag
     */
    public boolean checkTANExpiration(String tan) {
        return true;
    }

    /**
     * Check TAN redeemed.
     *
     * @param tan the TAN
     * @return TAN redeemed flag
     */
    public boolean checkTANRedeemed(String tan) {
        return true;
    }

    /**
     * Returns the a Valid Tan String
     *
     * @return a Valid Tan String
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
     * Check for existing TAN in the {@link CoronaVerficationTANRepository}.
     *
     * @param tan the TAN
     * @return flag for existing TAN
     */
    public boolean checkTANAlreadyExist(String tan) {
        return hashTanAndCheckAvailability(tan);
    }

    private CoronaVerificationTAN persistTan(String tan) {
        CoronaVerificationTAN newTan = generateCoronaVerificationTAN(tan);
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
        return tanRepository.existsByTanHash(tanHash);
    }

    private CoronaVerificationTAN generateCoronaVerificationTAN(String tan) {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime until = from.plusSeconds(TAN_VALID_IN_SECONDS);

        CoronaVerificationTAN coronaVerificationTAN = new CoronaVerificationTAN();
        coronaVerificationTAN.setTanHash(hashingService.hash(tan));
        coronaVerificationTAN.setValidFrom(from);
        coronaVerificationTAN.setValidUntil(until);
        coronaVerificationTAN.setRedeemed(false);

        return coronaVerificationTAN;
    }

}
