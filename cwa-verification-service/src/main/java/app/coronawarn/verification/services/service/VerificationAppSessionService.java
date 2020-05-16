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

import app.coronawarn.verification.services.domain.CoronaVerificationAppSession;
import app.coronawarn.verification.services.repository.AppSessionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;

/**
 * This class represents the CoronaVerficationAppSession service.
 *
 * @author T-Systems International GmbH
 */
@Component
public class VerificationAppSessionService {

    private static final Logger LOG = LogManager.getLogger();

    @Autowired
    private AppSessionRepository appSessionRepository;
    
    @Autowired
    private HashingService hashingService;    

    /**
     * Creates an AppSession-Entity.
     *
     * @param hashedGuid
     * @param hashedRegistrationToken
     * @return
     */
    public CoronaVerificationAppSession generateAppSession(String hashedGuid, String hashedRegistrationToken) {
        LOG.info("Create the app session entity with the created registration token and given guid.");
        CoronaVerificationAppSession appSession = new CoronaVerificationAppSession();
        appSession.setCreatedOn(LocalDateTime.now());
        appSession.setGuidHash(hashedGuid);
        appSession.setRegistrationTokenHash(hashedRegistrationToken);
        appSession.setTanGenerated(false);
        return appSession;
    }

    /**
     * Persists the specified entity of {@link CoronaVerficationAppSession}
     * instances.
     *
     * @param appSession the verification app session entity
     */
    public void saveAppSession(CoronaVerificationAppSession appSession) {
        LOG.info("VerficationAppSessionService start saveAppSession.");
        appSessionRepository.save(appSession);
    }

    /**
     * Check for existing Reg Token in the {@link AppSessionRepository}.
     *
     * @param registrationTokenHash the hashed registrationToken
     * @return flag for existing registrationToken
     */
    public boolean checkRegistrationTokenExists(String registrationTokenHash) {
        LOG.info("VerficationAppSessionService start checkRegistrationTokenExists.");
        CoronaVerificationAppSession appSession = new CoronaVerificationAppSession();
        appSession.setRegistrationTokenHash(registrationTokenHash);
        return appSessionRepository.exists(Example.of(appSession, ExampleMatcher.matchingAll()));
    }

    /**
     * Get existing CoronaVerificationAppSession for Reg Token from
     * {@link AppSessionRepository}.
     *
     * @param registrationToken the registrationToken
     * @return Optional CoronaVerificationAppSession
     */
    public Optional<CoronaVerificationAppSession> getAppSessionByToken(String registrationToken) {
        LOG.info("VerficationAppSessionService start getAppSessionByToken.");
        CoronaVerificationAppSession appSession = new CoronaVerificationAppSession();
        appSession.setRegistrationTokenHash(hashingService.hash(registrationToken));
        return appSessionRepository.findOne(Example.of(appSession, ExampleMatcher.matching()));
    }

    /**
     * Check for existing GUID Token in the {@link AppSessionRepository}.
     *
     * @param guid the hashed guid
     * @return flag for existing guid
     */
    public boolean checkGuidExists(String guid) {
        LOG.info("VerficationAppSessionService start checkRegistrationTokenExists.");
        CoronaVerificationAppSession appSession = new CoronaVerificationAppSession();
        appSession.setGuidHash(guid);
        return appSessionRepository.exists(Example.of(appSession, ExampleMatcher.matchingAll()));
    }

}
