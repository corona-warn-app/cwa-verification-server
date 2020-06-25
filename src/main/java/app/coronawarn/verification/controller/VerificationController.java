/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, T-Systems International GmbH
 *
 * Deutsche Telekom AG and all other contributors /
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

package app.coronawarn.verification.controller;

import app.coronawarn.verification.config.VerificationApplicationConfig;
import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.exception.VerificationServerException;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.AuthorizationToken;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.LabTestResult;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.RegistrationTokenKeyType;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.model.Tan;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.model.TeleTan;
import app.coronawarn.verification.model.TestResult;
import app.coronawarn.verification.service.AppSessionService;
import app.coronawarn.verification.service.JwtService;
import app.coronawarn.verification.service.TanService;
import app.coronawarn.verification.service.TestResultServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class represents the rest controller for the verification server.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/version/v1")
@Validated
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
   * The route to the teleTAN generation endpoint.
   */
  public static final String TELE_TAN_ROUTE = "/tan/teletan";
  
  @NonNull
  private final AppSessionService appSessionService;

  @NonNull
  private final TestResultServerService testResultServerService;

  @NonNull
  private final TanService tanService;

  @NonNull
  private final VerificationApplicationConfig verificationApplicationConfig;

  @NonNull
  private final JwtService jwtService;

  /**
   * This method generates a registrationToken by a hashed guid or a teleTAN.
   *
   * @param request {@link RegistrationTokenRequest}
   * @return RegistrationToken - the created registration token {@link RegistrationToken}
   */
  @Operation(
    summary = "Get registration Token",
    description = "Get a registration token by providing a SHA-256 hasehd GUID or a teleTAN"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "registration token generated."),
    @ApiResponse(responseCode = "400", description = "GUID/TeleTAN already exists.")})
  @PostMapping(value = REGISTRATION_TOKEN_ROUTE,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<RegistrationToken> generateRegistrationToken(
    @RequestBody @Valid RegistrationTokenRequest request) {
    String key = request.getKey();
    RegistrationTokenKeyType keyType = request.getKeyType();
    switch (keyType) {
      case GUID:
        return appSessionService.generateRegistrationTokenByGuid(key);
      case TELETAN:
        ResponseEntity<RegistrationToken> response = appSessionService.generateRegistrationTokenByTeleTan(key);
        Optional<VerificationTan> optional = tanService.getEntityByTan(key);
        if (optional.isPresent()) {
          VerificationTan teleTan = optional.get();
          teleTan.setRedeemed(true);
          tanService.saveTan(teleTan);
          return response;
        }
        throw new VerificationServerException(HttpStatus.BAD_REQUEST, "The teleTAN verification failed");
      default:
        throw new VerificationServerException(HttpStatus.BAD_REQUEST,
          "Unknown registration key type for registration token");
    }
  }

  /**
   * This method generates a transaction number by a Registration Token, if the state of the COVID-19 lab-test is
   * positive.
   *
   * @param registrationToken generated by a hashed guid or a teleTAN. {@link RegistrationToken}
   * @return A generated transaction number {@link Tan}.
   */
  @Operation(
    summary = "Generates a Tan",
    description = "Generates a TAN on input of Registration Token. With the TAN one can submit his Diagnosis keys"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Registration Token is valid"),
    @ApiResponse(responseCode = "400", description = "Registration Token does not exist")})
  @PostMapping(value = TAN_ROUTE,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Tan> generateTan(@Valid @RequestBody RegistrationToken registrationToken) {

    Optional<VerificationAppSession> actual
      = appSessionService.getAppSessionByToken(registrationToken.getRegistrationToken());
    if (actual.isPresent()) {
      VerificationAppSession appSession = actual.get();
      int tancountermax = verificationApplicationConfig.getAppsession().getTancountermax();
      if (appSession.getTanCounter() < tancountermax) {
        AppSessionSourceOfTrust appSessionSourceOfTrust = appSession.getSourceOfTrust();
        TanSourceOfTrust tanSourceOfTrust = TanSourceOfTrust.CONNECTED_LAB;
        switch (appSessionSourceOfTrust) {
          case HASHED_GUID:
            TestResult covidTestResult = testResultServerService.result(new HashedGuid(appSession.getHashedGuid()));
            if (covidTestResult.getTestResult() != LabTestResult.POSITIVE.getTestResult()) {
              throw new VerificationServerException(HttpStatus.BAD_REQUEST,
                "Tan cannot be created, caused by the non positive result of the labserver");
            }
            break;
          case TELETAN:
            tanSourceOfTrust = TanSourceOfTrust.TELETAN;
            break;
          default:
            throw new VerificationServerException(HttpStatus.BAD_REQUEST,
              "Unknown source of trust inside the appsession for the registration token");
        }
        appSession.incrementTanCounter();
        appSessionService.saveAppSession(appSession);
        String generatedTan = tanService.generateVerificationTan(tanSourceOfTrust);
        log.info("Returning the successfully generated tan.");
        return ResponseEntity.status(HttpStatus.CREATED).body(new Tan(generatedTan));
      }
      throw new VerificationServerException(HttpStatus.BAD_REQUEST,
        "The maximum of generating tans for this registration token is reached");
    }
    throw new VerificationServerException(HttpStatus.BAD_REQUEST,
      "VerificationAppSession not found for the registration token");
  }

  /**
   * Returns the test status of the COVID-19 test.
   *
   * @param registrationToken generated by a hashed guid {@link RegistrationToken}
   * @return the test result / status of the COVID-19 test, which can be POSITIVE, NEGATIVE, INVALID, PENDING or FAILED
   *     and will always be POSITIVE for a TeleTan
   */
  @Operation(
    summary = "COVID-19 test result for given RegistrationToken",
    description = "Gets the result of COVID-19 Test. "
      + "If the RegistrationToken belongs to a TeleTan the result is always positive"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Testresult retrieved")})
  @PostMapping(value = TESTRESULT_ROUTE,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )

  public ResponseEntity<TestResult> getTestState(@Valid @RequestBody RegistrationToken registrationToken) {
    Optional<VerificationAppSession> appSession =
      appSessionService.getAppSessionByToken(registrationToken.getRegistrationToken());
    if (appSession.isPresent()) {
      AppSessionSourceOfTrust sourceOfTrust = appSession.get().getSourceOfTrust();
      switch (sourceOfTrust) {
        case HASHED_GUID:
          String hash = appSession.get().getHashedGuid();
          log.info("Requested result for registration token with hashed Guid.");
          TestResult testResult = testResultServerService.result(new HashedGuid(hash));
          return ResponseEntity.ok(testResult);
        case TELETAN:
          return ResponseEntity.ok(new TestResult(LabTestResult.POSITIVE.getTestResult()));
        default:
          throw new VerificationServerException(HttpStatus.BAD_REQUEST,
            "Unknown source of trust inside the appsession for the registration token");
      }
    }
    log.info("The registration token doesn't exists.");
    throw new VerificationServerException(HttpStatus.BAD_REQUEST,
      "Returning the test result for the registration token failed");
  }

  /**
   * This provided REST method verifies the transaction number (TAN).
   *
   * @param tan - the transaction number, which needs to be verified {@link Tan}
   * @return HTTP 200, if the verification was successful. Otherwise HTTP 404.
   */
  @Operation(
    summary = "Verify provided Tan",
    description = "The provided Tan is verified to be formerly issued by the verification server"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Tan is valid an formerly issued by the verification server"),
    @ApiResponse(responseCode = "404", description = "Tan could not be verified")})
  @PostMapping(value = TAN_VERIFY_ROUTE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<?> verifyTan(@Valid @RequestBody Tan tan) {
    return tanService.getEntityByTan(tan.getTan())
      .filter(t -> t.canBeRedeemed(LocalDateTime.now()))
      .map(t -> {
        tanService.deleteTan(t);
        log.info("The Tan is valid.");
        return t;
      })
      .map(t -> ResponseEntity.ok().build())
      .orElseGet(() -> {
        log.info("The Tan is invalid.");
        throw new VerificationServerException(HttpStatus.NOT_FOUND, "No Tan found or Tan is invalid");
      });
  }

  /**
   * This method generates a valid teleTAN.
   *
   * @param authorization auth
   * @return a created teletan
   */
  @Operation(
    summary = "Request generation of a teleTAN",
    description = "A teleTAN is a human readable TAN with 7 characters which is supposed to be issued via call line"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "TeleTan created")})
  @PostMapping(value = TELE_TAN_ROUTE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<TeleTan> createTeleTan(
    @RequestHeader(JwtService.HEADER_NAME_AUTHORIZATION) @Valid AuthorizationToken authorization) {
    if (jwtService.isAuthorized(authorization.getToken())) {
      String teleTan = tanService.generateVerificationTeleTan();
      log.info("The teleTAN is generated.");
      return ResponseEntity.status(HttpStatus.CREATED).body(new TeleTan(teleTan));
    }
    throw new VerificationServerException(HttpStatus.UNAUTHORIZED, "JWT is invalid.");
  }

}
