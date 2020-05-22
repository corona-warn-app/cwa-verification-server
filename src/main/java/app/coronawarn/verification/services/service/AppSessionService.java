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

import app.coronawarn.verification.services.domain.VerificationAppSession;
import app.coronawarn.verification.services.repository.VerificationAppSessionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * This class represents the VerficationAppSession service.
 *
 * @author T-Systems International GmbH
 */
@Component
public class AppSessionService {

    /**
     * The logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * The {@link VerificationAppSessionRepository}.
     */
    @Autowired
    private VerificationAppSessionRepository appSessionRepository;

    /**
     * The {@link HashingService}.
     */
    @Autowired
    private HashingService hashingService;

    /**
     * Creates an AppSession-Entity.
     *
     * @param hashedGuid
     * @param registrationToken
     * @return
     */
    public VerificationAppSession generateAppSession(String hashedGuid, String registrationToken) {
        LOG.info("Create the app session entity with the created registration token and given guid.");
        VerificationAppSession appSession = new VerificationAppSession();
        appSession.setCreatedAt(LocalDateTime.now());
        appSession.setUpdatedAt(LocalDateTime.now());
        appSession.setGuidHash(hashedGuid);
        appSession.setRegistrationTokenHash(hashingService.hash(registrationToken));
        appSession.setTanGenerated(false);
        return appSession;
    }

    /**
     * This mehtod generates a randoom registration Token.
     *
     * @return
     */
    public String generateRegistrationToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Persists the specified entity of {@link VerificationAppSession}
     * instances.
     *
     * @param appSession the verification app session entity
     */
    public void saveAppSession(VerificationAppSession appSession) {
        LOG.info("VerficationAppSessionService start saveAppSession.");
        appSessionRepository.save(appSession);
    }

    /**
     * Check for existing Reg Token in the
     * {@link VerificationAppSessionRepository}.
     *
     * @param registrationTokenHash the hashed registrationToken
     * @return flag for existing registrationToken
     */
    public boolean checkRegistrationTokenExists(String registrationTokenHash) {
        LOG.info("VerficationAppSessionService start checkRegistrationTokenExists.");
        VerificationAppSession appSession = new VerificationAppSession();
        appSession.setRegistrationTokenHash(registrationTokenHash);
        return appSessionRepository.exists(Example.of(appSession, ExampleMatcher.matchingAll()));
    }

    /**
     * Get existing VerificationAppSession for Reg Token from
     * {@link VerificationAppSessionRepository}.
     *
     * @param registrationToken the registrationToken
     * @return Optional VerificationAppSession
     */
    public Optional<VerificationAppSession> getAppSessionByToken(String registrationToken) {
        LOG.info("VerficationAppSessionService start getAppSessionByToken.");
        VerificationAppSession appSession = new VerificationAppSession();
        appSession.setRegistrationTokenHash(hashingService.hash(registrationToken));
        return appSessionRepository.findOne(Example.of(appSession, ExampleMatcher.matching()));
    }

    /**
     * Check for existing GUID Token in the
     * {@link VerificationAppSessionRepository}.
     *
     * @param guid the hashed guid
     * @return flag for existing guid
     */
    public boolean checkGuidExists(String guid) {
        LOG.info("VerficationAppSessionService start checkRegistrationTokenExists.");
        VerificationAppSession appSession = new VerificationAppSession();
        appSession.setGuidHash(guid);
        return appSessionRepository.exists(Example.of(appSession, ExampleMatcher.matchingAll()));
    }

}
