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
package app.coronawarn.verification.services.controller;

import app.coronawarn.verification.services.domain.CoronaVerificationAppSession;
import app.coronawarn.verification.services.domain.CoronaVerificationState;
import app.coronawarn.verification.services.service.HashingService;
import app.coronawarn.verification.services.service.LabServerService;
import app.coronawarn.verification.services.service.VerificationAppSessionService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class represents the rest controller for the verification service.
 *
 */
@RestController
public class VerificationController {

    /**
     * The logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    @Autowired
    private VerificationAppSessionService appSessionService;

    @Autowired
    private HashingService hashingService;
    
    @Autowired
    private LabServerService labServerService;    

    /**
     * The default constructor.
     */
    public VerificationController() {
        LOG.info("Initializing " + VerificationController.class.getSimpleName() + ".");
    }

    /**
     * This method generates a registrationToken, which will get stored with the
     * guid.
     *
     * @param hashedGuid
     * @return the created registration token.
     */
    @RequestMapping(headers = {"content-type=application/json"},
            method = RequestMethod.POST, value = "/registrationToken")
    public ResponseEntity<String> generateRegistrationToken(@RequestBody String hashedGuid) {

        if (appSessionService.checkGuidExists(hashedGuid)) {
            LOG.warn("The registration token already exists for the hashed guid.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } else {
            LOG.info("Start generating a new registration token for the given hashed guid.");
            String registrationToken = UUID.randomUUID().toString();
            String hashedRegistrationToken = hashingService.hash(registrationToken);
            CoronaVerificationAppSession appSession = createAppSession(hashedGuid, hashedRegistrationToken);
            appSessionService.saveAppSession(appSession);
            return new ResponseEntity(registrationToken, HttpStatus.OK);
        }
    }

    /**
     * This method generates a transaction number (TAN), if the state of the
     * corona test is positive.
     *
     * @param registrationToken
     * @return A generated TAN (with the HTTP-state 201 Created). Otherwise the
     * HTTP-state 400 (Bad Request) will be returned, if an error occures.
     */
    @RequestMapping(headers = {"content-type=application/json"},
            method = RequestMethod.POST, value = "/tan")
    public ResponseEntity<CoronaVerificationAppSession> generateTAN(@RequestBody String registrationToken) {
        /* TODO: 
        1. Verify Registration Token, if Registration Token is invalid, exit with error HTTP 400
        2. Get test result from Lab Sever
        3. Verify whether test result is positive, otherwise exit with error HTTP 400
        4. generate TAN
            a) Generate random TAN
                - Check collision with existing TANs, if yes regenerate
        5. Persist TAN as entity TAN
        6. Update entity AppSession, mark as “used for TAN generation”
        7. Return TAN string
         */
        try {
            return new ResponseEntity(new CoronaVerificationAppSession().getRegistrationTokenHash(), HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Returns the test status of the COVID-19 test.
     *
     * @param registrationToken
     * @return the test result / status of the COVID-19 test, which can be
     * POSITIVE, NEGATIVE, INVALID, PENDING or FAILED
     */
    @RequestMapping(headers = {"content-type=application/json"},
            method = RequestMethod.POST, value = "/testresult")
    public ResponseEntity<CoronaVerificationState> getTestState(@RequestBody String registrationToken) {

        // “Get Test status” - Terminate the external API call  ------------ nachfragen???
        Optional<CoronaVerificationAppSession> actual = appSessionService.getAppSessionByToken(registrationToken);
        if(actual.isPresent()){
            // - Do call rate limiting, to avoid overload of external API - ---------prüfen
            String result = labServerService.callLabServerResult(actual.get().getGuidHash());
            return new ResponseEntity(result, HttpStatus.OK);
        }
        else{
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method verifies the transaction number (TAN).
     *
     * @param tan - the transaction number, which needs to be verified
     * @return HTTP-Status 200, if the verification was successfull. Otherwise
     * it will return HTTP 404.
     */
    @RequestMapping(headers = {"content-type=application/json"},
            method = RequestMethod.POST, value = "/tan/verify")
    public ResponseEntity verifyTAN(@RequestBody String tan) {
        /*TODO: 
        1. Verify parameter TAN for syntax constraints
        2. Obtain entity TAN by provided tan string
        3. If entity TAN does not exist, exit with error HTTP 404
        4. If current time is not between entity TAN.vaildFrom and TAN.validUntil,
        exit with error HTTP 404
         */
        try {
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * This method creates a TeleTAN.
     *
     * @return a created teletan
     */
    @RequestMapping(method = RequestMethod.POST, value = "/tan/teletan")
    public ResponseEntity createTeleTAN() {
        return new ResponseEntity(HttpStatus.CREATED);
    }

    /**
     * Creates an AppSession-Entity.
     *
     * @param hashedGuid
     * @param hashedRegistrationToken
     * @return
     */
    private CoronaVerificationAppSession createAppSession(String hashedGuid, String hashedRegistrationToken) {
        LOG.info("Create the app session entity with the created registration token and given guid.");
        CoronaVerificationAppSession appSession = new CoronaVerificationAppSession();
        appSession.setCreatedOn(LocalDateTime.now());
        appSession.setGuidHash(hashedGuid);
        appSession.setRegistrationTokenHash(hashedRegistrationToken);
        appSession.setTanGenerated(false);
        return appSession;
    }
}
