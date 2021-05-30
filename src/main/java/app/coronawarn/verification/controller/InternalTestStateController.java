package app.coronawarn.verification.controller;

import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.exception.VerificationServerException;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.InternalTestResult;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.TestResult;
import app.coronawarn.verification.service.AppSessionService;
import app.coronawarn.verification.service.TestResultServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Optional;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;


/**
 * This class represents the rest controller for requests regarding test states.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/version/v1")
@Validated
@Profile("internal")
public class InternalTestStateController {

  /**
   * The route to the test status of the COVID-19 test endpoint.
   */
  public static final String TESTRESULT_ROUTE = "/testresult";

  private final AppSessionService appSessionService;

  private final TestResultServerService testResultServerService;

  /**
   * Returns the test status of the COVID-19 test.
   *
   * @param registrationToken generated by a hashed guid {@link RegistrationToken}
   * @return result of the test, which can be POSITIVE, NEGATIVE, INVALID, PENDING, FAILED,
   *     quick-test-POSITIVE, quick-test-NEGATIVE, quick-test-INVALID, quick-test-PENDING or quick-test-FAILED
   *     will be POSITIVE for TeleTan
   */
  @Operation(
    summary = "COVID-19 test result for given RegistrationToken",
    description = "Gets the result of COVID-19 Test. "
      + "If the RegistrationToken belongs to a TeleTan the result is always positive"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Testresult retrieved"),
    @ApiResponse(responseCode = "403", description = "RegistrationToken is issued for TeleTan"),
    @ApiResponse(responseCode = "404", description = "RegistrationToken not found")
  })
  @PostMapping(value = TESTRESULT_ROUTE,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<InternalTestResult> getTestState(@Valid @RequestBody RegistrationToken registrationToken) {

    Optional<VerificationAppSession> appSession =
      appSessionService.getAppSessionByToken(registrationToken.getRegistrationToken());

    if (appSession.isPresent()) {
      AppSessionSourceOfTrust sourceOfTrust = appSession.get().getSourceOfTrust();

      switch (sourceOfTrust) {
        case HASHED_GUID:
          HashedGuid hash = new HashedGuid(appSession.get().getHashedGuid());
          TestResult testResult = testResultServerService.result(hash);

          // Check DOB Hash if present
          if (appSession.get().getHashedGuidDob() != null) {
            HashedGuid hashDob = new HashedGuid(appSession.get().getHashedGuidDob());
            TestResult testResultDob = testResultServerService.result(hashDob);

            // TRS will always respond with a TestResult so we have to check if both results are equal
            if (testResultDob.getTestResult() != testResult.getTestResult()) {
              // given DOB Hash is invalid
              throw new VerificationServerException(HttpStatus.FORBIDDEN,
                "TestResult of dob hash does not equal to TestResult of hash");
            }
          }
          log.debug("Result {}", testResult);
          log.info("The result for registration token based on hashed Guid will be returned.");

          return ResponseEntity.ok(new InternalTestResult(
            testResult.getTestResult(),
            testResult.getSc(),
            testResult.getLabId(),
            testResult.getResponsePadding(),
            appSession.get().getHashedGuid()));

        case TELETAN:
          log.info("Internal TestState is not allowed for TeleTan Token.");
          throw new VerificationServerException(HttpStatus.FORBIDDEN,
            "Internal TestState is not allowed for TeleTan Token.");
        default:
          throw new VerificationServerException(HttpStatus.BAD_REQUEST,
            "Unknown source of trust inside the appsession for the registration token");
      }
    }
    log.info("The registration token doesn't exist.");
    throw new VerificationServerException(HttpStatus.NOT_FOUND,
      "Registration Token not found");
  }
}
