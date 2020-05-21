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

import app.coronawarn.verification.services.common.AppSessionSourceOfTrust;
import app.coronawarn.verification.services.common.RegistrationToken;
import app.coronawarn.verification.services.common.RegistrationTokenKeyType;
import app.coronawarn.verification.services.domain.VerificationAppSession;
import app.coronawarn.verification.services.repository.VerificationAppSessionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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
     * The {@link TanService}.
     */
    @Autowired
    private TanService tanService;
    /**
     * Creates an AppSession-Entity.
     *
     * @param registrationToken Token for registration
     * @return appSession for registrationToken
     */
    public VerificationAppSession generateAppSession(String registrationToken) {
        LOG.info("Create the app session entity with the created registration token.");
        VerificationAppSession appSession = new VerificationAppSession();
        appSession.setCreatedOn(LocalDateTime.now());
        appSession.setRegistrationTokenHash(hashingService.hash(registrationToken));
        appSession.setTanCounter(0);
        return appSession;
    }

   
    private String generateRegistrationToken() {
        return UUID.randomUUID().toString();
    }
    
     /**
     * This method generates a registration Token by a guid or a teletan.
     *
     * @param key the guid or teletan
     * @param keyType the key type {@link RegistrationTokenKeyType}
     * @return an {@link ResponseEntity<RegistrationToken>}
     */
    public ResponseEntity<RegistrationToken> generateRegistrationToken(String key, RegistrationTokenKeyType keyType) {
        String registrationToken;
        VerificationAppSession appSession;
        
        switch (keyType) {
            case GUID: 
                String hashedGuid = key;
                if (hashingService.isHashValid(key)) {
                    if (checkRegistrationTokenAlreadyExistsForGuid(hashedGuid)) {
                        LOG.warn("The registration token already exists for the hashed guid.");
                    } else {
                        LOG.info("Start generating a new registration token for the given hashed guid.");
                        registrationToken = generateRegistrationToken();
                        appSession = generateAppSession(registrationToken);
                        appSession.setGuidHash(hashedGuid);
                        appSession.setSourceOfTrust(AppSessionSourceOfTrust.HASHED_GUID.getSourceName());
                        saveAppSession(appSession);
                        return new ResponseEntity(new RegistrationToken(registrationToken), HttpStatus.CREATED);
                    }
                }
                break;
            case TELETAN:
                String teleTan = key;
                if(tanService.isTeleTanValid(teleTan)){
                    if (checkRegistrationTokenAlreadyExistForTeleTan(teleTan)) {
                        LOG.warn("The registration token already exists for this TeleTAN.");
                    } else {
                        LOG.info("Start generating a new registration token for the given tele TAN.");
                        registrationToken = generateRegistrationToken();
                        appSession = generateAppSession(registrationToken);
                        appSession.setTeleTanHash(hashingService.hash(teleTan));
                        appSession.setSourceOfTrust(AppSessionSourceOfTrust.TELETAN.getSourceName());
                        saveAppSession(appSession);
                        return new ResponseEntity(new RegistrationToken(registrationToken), HttpStatus.CREATED);
                    }
                }
                else {
                    LOG.warn("The Tele Tan supplied is not valid.");
                }
                break;
            default: 
                break;
        } 
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
     * Check for existing hashed GUID Token in the
     * {@link VerificationAppSessionRepository}.
     *
     * @param hashedGuid the hashed guid
     * @return flag for existing guid
     */
    public boolean checkRegistrationTokenAlreadyExistsForGuid(String hashedGuid) {
        LOG.info("VerficationAppSessionService start checkRegistrationTokenExists.");
        VerificationAppSession appSession = new VerificationAppSession();
        appSession.setGuidHash(hashedGuid);
        return appSessionRepository.exists(Example.of(appSession, ExampleMatcher.matchingAll()));
    }

    /**
     * Check for existing hashed TeleTAN in the
     * {@link VerificationAppSessionRepository}.
     *
     * @param teleTan the teleTAN
     * @return flag for existing teleTAN
     */
    public boolean checkRegistrationTokenAlreadyExistForTeleTan(String teleTan) {
        LOG.info("VerficationAppSessionService start checkTeleTanAlreadyExistForTeleTan.");
        VerificationAppSession appSession = new VerificationAppSession();
        appSession.setRegistrationTokenHash(hashingService.hash(teleTan));
        return appSessionRepository.exists(Example.of(appSession, ExampleMatcher.matchingAll()));
    }

}
