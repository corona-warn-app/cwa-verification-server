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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * This class represents the rest controller for the verification server.
 */
@RestController
@RequestMapping("/version/v1")
public class VerificationController {
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
    /**
     * The logger.
     */
    private static final Logger LOG = LogManager.getLogger();

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
    @Operation(
        summary = "Get registration Token",
        description = "Get a registration token by providing a SHA-256 hasehd GUID or a TeleTAN"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GUID/TeleTAN found"),
        @ApiResponse(responseCode = "400", description = "GUID/TeleTAN already exists."),
    })
    @PostMapping(value = REGISTRATION_TOKEN_ROUTE,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RegistrationToken> generateRegistrationToken(@RequestBody HashedGuid hashedGuid) {
        //TODO: Add consumption of TeleTAN to this request 
        if (appSessionService.checkGuidExists(hashedGuid.getHashedGUID())) {
            LOG.warn("The registration token already exists for the hashed guid.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            LOG.info("Start generating a new registration token for the given hashed guid.");
            String registrationToken = appSessionService.generateRegistrationToken();
            VerificationAppSession appSession = appSessionService.generateAppSession(hashedGuid.getHashedGUID(), registrationToken);
            appSessionService.saveAppSession(appSession);
            return ResponseEntity.ok(new RegistrationToken(registrationToken));
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
    @Operation(
        summary = "Generates a Tan",
        description = "Generates a TAN on input of Registration Token. With the TAN one can submit his Diagnosis keys"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Registration Token is valid"),
        @ApiResponse(responseCode = "400", description = "Registration Token does not exist"),
    })
    @PostMapping(value = TAN_ROUTE,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
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
                        return ResponseEntity.status(HttpStatus.CREATED).body(new Tan(generatedTAN));
                    }
                }
                break;
            //TODO: Move the Teletan consumption option to the /registrationToken request, the /tan request only takes registrationTokens
            case TELETAN:
                Optional<VerificationTan> teleTANEntity = tanService.getEntityByTan(key);
                if (teleTANEntity.isPresent() && !teleTANEntity.get().isRedeemed()) {
                    generatedTAN = tanService.generateVerificationTan();
                    VerificationTan teleTAN = teleTANEntity.get();

                    teleTAN.setRedeemed(true);
                    tanService.saveTan(teleTAN);
                    return ResponseEntity.status(HttpStatus.CREATED).body(new Tan(generatedTAN));
                }
                LOG.info("The given teleTAN is invalid.");
                break;
            default:
                break;
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    /**
     * Returns the test status of the COVID-19 test.
     *
     * @param registrationToken
     * @return the test result / status of the COVID-19 test, which can be
     * POSITIVE, NEGATIVE, INVALID, PENDING or FAILED
     */
    @Operation(
        summary = "COVID-19 test result",
        description = "Gets the result of COVID-19 Test."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Testresult retrieved"),
    })
    @PostMapping(value = TESTRESULT_ROUTE,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TestResult> getTestState(@RequestBody RegistrationToken registrationToken) {

        Optional<VerificationAppSession> actual = appSessionService.getAppSessionByToken(registrationToken.getRegistrationToken());
        if (actual.isPresent()) {
            //TODO Exception Handling 404 from LabServer
            TestResult result = labServerService.result(new Guid(actual.get().getGuidHash()));
            return ResponseEntity.ok(result);
        } else {
            LOG.info("The registration token is invalid.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * This provided REST method verifies the transaction number (TAN).
     *
     * @param tan - the transaction number, which needs to be verified
     * @return HTTP-Status 200, if the verification was successfull.
     * Otherwise return HTTP 404.
     */
    @Operation(
        summary = "Verify provided TAN",
        description = "The provided Tan is verified to be formerly issued by the verification server"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "TAN is valid an formerly issued by the verification server"),
        @ApiResponse(responseCode = "404", description = "TAN could not be verified"),
    })
    @PostMapping(value = TAN_VERIFY_ROUTE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
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
        return ResponseEntity.status(verified ? HttpStatus.OK : HttpStatus.NOT_FOUND).build();
    }

    /**
     * This method creates a TeleTAN.
     *
     * @return a created teletan
     */
    @Operation(
        summary = "Request generation of a TeleTAN",
        description = "A TeleTAN is a human readable TAN with 7 characters which is supposed to be issued via call line"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "TeleTAN created"),
    })
    @PostMapping(TELE_TAN_ROUTE)
    public ResponseEntity createTeleTAN() {
        //TODO implement if the clarification about JWT is done
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
