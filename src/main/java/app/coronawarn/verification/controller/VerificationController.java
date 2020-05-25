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

import app.coronawarn.verification.client.HashedGuid;
import app.coronawarn.verification.client.TestResult;
import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.LabTestResult;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.RegistrationTokenKeyType;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.model.Tan;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.service.AppSessionService;
import app.coronawarn.verification.service.LabServerService;
import app.coronawarn.verification.service.TanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class represents the rest controller for the verification server.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
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

  @Value("${appsession.tancountermax}")
  private Integer tanCounterMax;

  private final AppSessionService appSessionService;

  private final LabServerService labServerService;

  private final TanService tanService;

  /**
   * This method generates a registrationToken by a hashed guid or a teleTan.
   *
   * @param request {@link RegistrationTokenRequest}
   * @return RegistrationToken - the created registration token {@link RegistrationToken}
   */
  @Operation(
    summary = "Get registration Token",
    description = "Get a registration token by providing a SHA-256 hasehd GUID or a TeleTAN"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "registration token generated."),
    @ApiResponse(responseCode = "400", description = "GUID/TeleTAN already exists."),
  })
  @PostMapping(value = REGISTRATION_TOKEN_ROUTE,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<RegistrationToken> generateRegistrationToken(@RequestBody RegistrationTokenRequest request) {
    String key = request.getKey();
    RegistrationTokenKeyType keyType = request.getKeyType();

    if (keyType == RegistrationTokenKeyType.TELETAN) {
      if (tanService.verifyTeleTan(key)) {
        ResponseEntity<RegistrationToken> response = appSessionService.generateRegistrationToken(key, keyType);
        Optional<VerificationTan> optional = tanService.getEntityByTan(key);
        if (optional.isPresent()) {
          VerificationTan teleTan = optional.get();
          teleTan.setRedeemed(true);
          tanService.saveTan(teleTan);
          return response;
        } else {
          log.warn("Teletan is not found");
        }
      }
    } else {
      return appSessionService.generateRegistrationToken(key, keyType);
    }
    return ResponseEntity.badRequest().build();
  }

  /**
   * This method generates a transaction number by a Registration Token, if
   * the state of the COVID-19 lab-test is positive.
   *
   * @param registrationToken generated by a hashed guid or a teleTan. {@link RegistrationToken}
   * @return A generated TAN (with the HTTP-state 201 Created). Otherwise the
   *     HTTP-state 400 (Bad Request) will be returned, if an error occurs.
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
  public ResponseEntity<Tan> generateTan(@RequestBody RegistrationToken registrationToken) {

    Optional<VerificationAppSession> actual =
      appSessionService.getAppSessionByToken(registrationToken.getRegistrationToken());
    if (actual.isPresent()) {
      VerificationAppSession appSession = actual.get();
      if (appSession.getTanCounter() < tanCounterMax) {
        String sourceOfTrust = appSession.getSourceOfTrust();
        if (AppSessionSourceOfTrust.HASHED_GUID.getSourceName().equals(sourceOfTrust)) {
          sourceOfTrust = TanSourceOfTrust.CONNECTED_LAB.getSourceName();
          TestResult covidTestResult = labServerService.result(new HashedGuid(appSession.getHashedGuid()));
          if (covidTestResult.getTestResult() != LabTestResult.POSITIVE.getTestResult()) {
            return ResponseEntity.badRequest().build();
          }
        } else if (AppSessionSourceOfTrust.TELETAN.getSourceName().equals(sourceOfTrust)) {
          sourceOfTrust = TanSourceOfTrust.TELETAN.getSourceName();
        } else {
          return ResponseEntity.badRequest().build();
        }
        String generatedTan = tanService.generateVerificationTan(sourceOfTrust);
        appSession.incrementTanCounter();
        appSessionService.saveAppSession(appSession);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Tan(generatedTan));
      }
    }
    return ResponseEntity.badRequest().build();
  }

  /**
   * Returns the test status of the COVID-19 test.
   *
   * @param registrationToken generated by a hashed guid {@link RegistrationToken}
   * @return the test result / status of the COVID-19 test, which can be POSITIVE, NEGATIVE, INVALID, PENDING or FAILED
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
    
    return appSessionService.getAppSessionByToken(registrationToken.getRegistrationToken())
        .map(it -> it.getHashedGuid())
        .map(HashedGuid::new)
        .map(labServerService::result)
        .map(ResponseEntity::ok)
        .orElseGet(() -> {
          log.info("The registration token is invalid.");
          return ResponseEntity.badRequest().build();
        });
  }

  /**
   * This provided REST method verifies the transaction number (TAN).
   *
   * @param tan - the transaction number, which needs to be verified {@link Tan}
   * @return HTTP-Status 200, if the verification was successful. Otherwise return HTTP 404.
   */
  @Operation(
    summary = "Verify provided Tan",
    description = "The provided Tan is verified to be formerly issued by the verification server"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Tan is valid an formerly issued by the verification server"),
    @ApiResponse(responseCode = "404", description = "Tan could not be verified"),
  })
  @PostMapping(value = TAN_VERIFY_ROUTE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<?> verifyTan(@RequestBody Tan tan) {

    //TODO TAN syntax constraints
    if (!tanService.syntaxVerification(tan.getTan())) {
      return ResponseEntity.notFound().build();
    }
      
     return tanService.getEntityByTan(tan.getTan())
        .filter(cvtan -> cvtan.canBeRedeemed(LocalDateTime.now()))
        .map(cvtan -> {
          cvtan.setRedeemed(true);
          return tanService.saveTan(cvtan);
        })
        .map(it -> ResponseEntity.ok().build())
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * This method creates a TeleTan.
   *
   * @return a created teletan
   */
  @Operation(
    summary = "Request generation of a TeleTan",
    description = "A TeleTan is a human readable TAN with 7 characters which is supposed to be issued via call line"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "TeleTan created"),
  })
  @PostMapping(TELE_TAN_ROUTE)
  public ResponseEntity<Void> createTeleTan() {
    // TODO implement if the clarification about communication is done
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
