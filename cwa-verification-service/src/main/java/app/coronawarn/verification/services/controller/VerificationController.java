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
import app.coronawarn.verification.services.domain.CoronaVerificationTAN;
import app.coronawarn.verification.services.domain.TANRequest;
import app.coronawarn.verification.services.service.HashingService;
import app.coronawarn.verification.services.service.LabServerService;
import app.coronawarn.verification.services.service.TanService;
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
public class VerificationController
{

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

    @Autowired
    private TanService tanService;

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
            CoronaVerificationAppSession appSession = appSessionService.generateAppSession(hashedGuid, hashedRegistrationToken);
            appSessionService.saveAppSession(appSession);
            return new ResponseEntity(registrationToken, HttpStatus.OK);
        }
    }

    /**
     * This method generates a transaction number (REGISTRATION_TOKEN), if the
     * state of the corona test is positive.
     *
     * @param request The request with the two parameters: key and keyType.
     * @return A generated REGISTRATION_TOKEN (with the HTTP-state 201 Created).
     * Otherwise the HTTP-state 400 (Bad Request) will be returned, if an error
     * occures.
     */
    @RequestMapping(headers = {"content-type=application/json"},
            method = RequestMethod.POST, value = "/tan")
    public ResponseEntity<String> generateTAN(@RequestBody TANRequest request) {
        /* TODO:
        1. Verify Registration Token, if Registration Token is invalid, exit with error HTTP 400
        2. Get test result from Lab Sever
        3. Verify whether test result is positive, otherwise exit with error HTTP 400
        4. generate REGISTRATION_TOKEN
        a) Generate random REGISTRATION_TOKEN
        - Check collision with existing TANs, if yes regenerate
        5. Persist REGISTRATION_TOKEN as entity REGISTRATION_TOKEN
        6. Update entity AppSession, mark as “used for REGISTRATION_TOKEN generation”
        7. Return REGISTRATION_TOKEN string
        
        Anpassungen noch von Alex übernehmen:
        Body: { 
            "key": "<<key>>",
            "keyType": “teleTAN||Token” 
            }

         */
        String key = request.getKey();
        CoronaVerificationTAN generatedTAN;
        switch(request.getKeyType()) {
            case TOKEN: 
                Integer covidTestResult = getTestState(key).getBody();
                if (covidTestResult != null) {
                    
                    //HASH HERE!!
                    CoronaVerificationAppSession appSession = 
                            appSessionService.getAppSessionByToken(key).get();
                    if (covidTestResult.equals(CoronaVerificationState.POSITIVE.getStateValue())
                            && !appSession.isTanGenerated()) {
                        generatedTAN = tanService.generateCoronaVerificationTAN();
                        appSession.setTanGenerated(true);
                        appSessionService.saveAppSession(appSession);
                        return new ResponseEntity(generatedTAN, HttpStatus.CREATED);
                    }
                }
                break;
            case TELETAN:
                /* TODO: TeleTAN
                    1.	Verify teleTAN
                        - If validation fails return HTTP 400
                    2. Generate TAN
                    3. Mark teleTAN as redeemed
                    4. Return TAN with HTTP 201
                */
                //hash here!!
                Optional<CoronaVerificationTAN> teleTANEntity = tanService.getTANByHashedTAN(key);
                if (teleTANEntity.isPresent() && !teleTANEntity.get().isRedeemed()) {
                    generatedTAN = tanService.generateCoronaVerificationTAN();
                    CoronaVerificationTAN teleTAN = teleTANEntity.get();
                    teleTAN.setRedeemed(true);
                    tanService.saveTan(teleTAN);
                    return new ResponseEntity(generatedTAN, HttpStatus.CREATED);
                }
                break;
            default: 
                break;
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<Integer> getTestState(@RequestBody String registrationToken) {

        Optional<CoronaVerificationAppSession> actual = appSessionService.getAppSessionByToken(registrationToken);
        if (actual.isPresent()) {
            //TODO  - call rate limiting, to avoid overload of external API - --------- check by Julius
            Integer result = labServerService.callLabServerResult(actual.get().getGuidHash());
            return new ResponseEntity(result, HttpStatus.OK);
        } else {
            LOG.info("The registration token is invalid.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method verifies the transaction number (TAN).
     *
     * @param tan - the transaction number, which needs to be verified
     * @return HTTP-Status 200, if the verification was successfull. 
     * Otherwise it will return HTTP 404.
     */
    @RequestMapping(headers = {"content-type=application/json"}, method = RequestMethod.POST, value = "/tan/verify")
    public ResponseEntity<String> verifyTAN(@RequestBody String tan) {

        HttpStatus ret = HttpStatus.NOT_FOUND;
        //TODO syntax constraints from Julius
        boolean verified = tanService.syntaxVerification(tan);

        if (verified) {
            //TODO change in one DB call
            boolean tanExist = tanService.checkTANAlreadyExist(tan);
            if (tanExist) {
                boolean tanExpiry = tanService.checkTANExpiration(tan);
                boolean tanRedeemed = tanService.checkTANRedeemed(tan);
                if (tanExpiry && !tanRedeemed) {
                    tanService.markTANRedeemed(tan);
                    ret = HttpStatus.OK;
                }
            }
        }
        return new ResponseEntity(ret);
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
}
