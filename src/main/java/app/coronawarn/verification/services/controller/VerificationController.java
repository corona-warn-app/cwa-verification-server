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

import app.coronawarn.verification.services.client.Guid;
import app.coronawarn.verification.services.client.LabServerService;
import app.coronawarn.verification.services.client.TestResult;
import app.coronawarn.verification.services.common.HashedGuid;
import app.coronawarn.verification.services.common.LabTestResult;
import app.coronawarn.verification.services.common.RegistrationToken;
import app.coronawarn.verification.services.common.Tan;
import app.coronawarn.verification.services.common.TanRequest;
import app.coronawarn.verification.services.domain.VerificationAppSession;
import app.coronawarn.verification.services.domain.VerificationTan;
import app.coronawarn.verification.services.service.AppSessionService;
import app.coronawarn.verification.services.service.TanService;
import io.swagger.annotations.ApiOperation;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class represents the rest controller for the verification server.
 *
 */
@RestController
@RequestMapping("/version/v1")
public class VerificationController
{
    /**
     * The logger.
     */
    private static final Logger LOG = LogManager.getLogger();
    
    /**
     * The route to the token registration endpoint.
     */
    public static final String REGISTRATION_TOKEN_ROUTE = "/registrationToken";
    
    /**
     * The route to the tan generation endpoint.
     */
    public static final String TAN_ROUTE = "/tan";
    
    /**
     * The route to the test status of the COVID-19 test endpoint.
     */
    public static final String TESTRESULT_ROUTE = "/testresult";    
    
    /**
     * The route to the tan verification endpoint.
     */
    public static final String TAN_VERIFY_ROUTE = "/tan/verify";
    
    /**
     * The route to the tele tan generation endpoint.
     */
    public static final String TELE_TAN_ROUTE = "/tan/teletan";

    @Autowired
    private AppSessionService appSessionService;

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
     * This method generates a registrationToken.
     *
     * @param hashedGuid
     * @return RegistrationToken - the created registration token.
     */
    @ApiOperation(value = "Generates and return a registration token", response = RegistrationToken.class)
    @PostMapping(REGISTRATION_TOKEN_ROUTE)
    public ResponseEntity<RegistrationToken> generateRegistrationToken(@RequestBody HashedGuid hashedGuid) {

        if (appSessionService.checkGuidExists(hashedGuid.getHashedGUID())) {
            LOG.warn("The registration token already exists for the hashed guid.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } else {
            LOG.info("Start generating a new registration token for the given hashed guid.");
            String registrationToken = appSessionService.generateRegistrationToken();
            VerificationAppSession appSession = appSessionService.generateAppSession(hashedGuid.getHashedGUID(), registrationToken);
            appSessionService.saveAppSession(appSession);
            return new ResponseEntity(new RegistrationToken(registrationToken), HttpStatus.OK);
        }
    }

    /**
     * This method generates a transaction number by a TeleTAN or Registration Token, if the
     * state of the COVID-19 lab-test is positive.
     *
     * @param request The request with the two parameters: key and keyType.
     * @return A generated TAN (with the HTTP-state 201 Created).
     * Otherwise the HTTP-state 400 (Bad Request) will be returned, if an error
     * occures.
     */
    @ApiOperation(value = "Generates and return a transaction number by a TeleTAN or Registration Token", response = Tan.class)
    @PostMapping(TAN_ROUTE)
    public ResponseEntity<Tan> generateTAN(@RequestBody TanRequest request) {

        String key = request.getKey();
        String generatedTAN;
        switch (request.getKeyType()) {
            case TOKEN:
                TestResult covidTestResult = getTestState(new RegistrationToken(key)).getBody();
                if (covidTestResult != null) {
                    VerificationAppSession appSession = appSessionService.getAppSessionByToken(key).get();
                    if (covidTestResult.getTestResult() == LabTestResult.POSITIVE.getTestResult() && !appSession.isTanGenerated()) {
                        generatedTAN = tanService.generateVerificationTan();
                        appSession.setTanGenerated(true);
                        appSessionService.saveAppSession(appSession);
                        return new ResponseEntity(new Tan(generatedTAN), HttpStatus.CREATED);
                    }
                }
                break;
            case TELETAN:
                Optional<VerificationTan> teleTANEntity = tanService.getEntityByTan(key);
                if (teleTANEntity.isPresent() && !teleTANEntity.get().isRedeemed()) {
                    generatedTAN = tanService.generateVerificationTan();
                    VerificationTan teleTAN = teleTANEntity.get();

                    teleTAN.setRedeemed(true);
                    tanService.saveTan(teleTAN);
                    return new ResponseEntity(new Tan(generatedTAN), HttpStatus.CREATED);
                }
                LOG.info("The given teleTAN is invalid.");
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
    @ApiOperation(value = "Returns the test status of the COVID-19 test", response = TestResult.class)
    @PostMapping(TESTRESULT_ROUTE)
    public ResponseEntity<TestResult> getTestState(@RequestBody RegistrationToken registrationToken) {

        Optional<VerificationAppSession> actual = appSessionService.getAppSessionByToken(registrationToken.getRegistrationToken());
        if (actual.isPresent()) {
            //TODO Exception Handling 404 from LabServer
            TestResult result = labServerService.result(new Guid(actual.get().getGuidHash()));
            return new ResponseEntity(result, HttpStatus.OK);
        } else {
            LOG.info("The registration token is invalid.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This provided REST method verifies the transaction number (TAN).
     *
     * @param tan - the transaction number, which needs to be verified
     * @return HTTP-Status 200, if the verification was successfull. 
     * Otherwise return HTTP 404.
     */
    @ApiOperation(value = "Verifies the transaction number - TAN.")
    @PostMapping(TAN_VERIFY_ROUTE)
    public ResponseEntity<Void> verifyTAN(@RequestBody Tan tan) {

        boolean verified = false;
        //TODO TAN syntax constraints from Julius
        boolean syntaxVerified = tanService.syntaxVerification(tan.getTan());

        if (syntaxVerified) {
            Optional<VerificationTan> optional = tanService.getEntityByTan(tan.getTan());
            if (optional.isPresent()) {
                VerificationTan cvtan = optional.get();
                LocalDateTime dateTimeNow = LocalDateTime.now();
                boolean tanTimeValid = dateTimeNow.isAfter(cvtan.getValidFrom()) && dateTimeNow.isBefore(cvtan.getValidUntil());
                boolean tanRedeemed = cvtan.isRedeemed();
                if (tanTimeValid && !tanRedeemed) {
                    cvtan.setRedeemed(true);
                    tanService.saveTan(cvtan);
                    verified = true;
                }
            }
        }
        return new ResponseEntity(verified ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    /**
     * This method creates a TeleTAN.
     *
     * @return a created teletan
     */
    @ApiOperation(value = "Creates a TeleTAN")
    @PostMapping(TELE_TAN_ROUTE)
    public ResponseEntity createTeleTAN() {
        //TODO implement if the clarification about JWT is done
        return new ResponseEntity(HttpStatus.CREATED);
    }
}