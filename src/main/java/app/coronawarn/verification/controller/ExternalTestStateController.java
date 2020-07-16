package app.coronawarn.verification.controller;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.exception.VerificationServerException;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.LabTestResult;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.TestResult;
import app.coronawarn.verification.service.AppSessionService;
import app.coronawarn.verification.service.FakeDelayService;
import app.coronawarn.verification.service.TestResultServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
@Profile("external")
public class ExternalTestStateController {

  /**
   * The route to the test status of the COVID-19 test endpoint.
   */
  public static final String TESTRESULT_ROUTE = "/testresult";

  private static final String RESULT_PADDING = "";

  private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(4);

  @NonNull
  private final ExternalFakeRequestController fakeRequestController;

  @NonNull
  private final AppSessionService appSessionService;

  @NonNull
  private final TestResultServerService testResultServerService;

  @NonNull
  private final FakeDelayService fakeDelayService;

  /**
   * Returns the test status of the COVID-19 test with cwa-fake header.
   *
   * @param registrationToken generated by a hashed guid {@link RegistrationToken}
   * @return the test result / status of the COVID-19 test, which can be POSITIVE, NEGATIVE, INVALID, PENDING or FAILED
   * and will always be POSITIVE for a TeleTan
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
  public DeferredResult<ResponseEntity<TestResult>> getTestState(
    @Valid @RequestBody RegistrationToken registrationToken,
    @RequestHeader(value = "cwa-fake", required = false) String fake) {
    if (fake != null) {
      if (fake.equals("1")) {
        return fakeRequestController.getTestState(registrationToken);
      }
    }
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    Optional<VerificationAppSession> appSession =
      appSessionService.getAppSessionByToken(registrationToken.getRegistrationToken());
    if (appSession.isPresent()) {
      AppSessionSourceOfTrust sourceOfTrust = appSession.get().getSourceOfTrust();
      DeferredResult<ResponseEntity<TestResult>> deferredResult = new DeferredResult<>();

      switch (sourceOfTrust) {
        case HASHED_GUID:
          String hash = appSession.get().getHashedGuid();
          TestResult testResult = testResultServerService.result(new HashedGuid(hash));
          log.debug("Result {}",testResult);
          log.info("The result for registration token based on hashed Guid will be returned.");
          stopWatch.stop();
          fakeDelayService.updateFakeTestRequestDelay(stopWatch.getTotalTimeMillis());
          scheduledExecutor.schedule(() -> deferredResult.setResult(ResponseEntity.ok(testResult)), 0, MILLISECONDS);
          return deferredResult;
        case TELETAN:
          log.info("The result for registration token based on teleTAN will be returned.");
          stopWatch.stop();
          fakeDelayService.updateFakeTestRequestDelay(stopWatch.getTotalTimeMillis());
          scheduledExecutor.schedule(() -> deferredResult.setResult(ResponseEntity.ok(
            new TestResult(LabTestResult.POSITIVE.getTestResult()))), 0, MILLISECONDS);
          return deferredResult;
        default:
          throw new VerificationServerException(HttpStatus.BAD_REQUEST,
            "Unknown source of trust inside the appsession for the registration token");
      }
    }
    log.info("The registration token doesn't exists.");
    throw new VerificationServerException(HttpStatus.BAD_REQUEST,
      "Returning the test result for the registration token failed");
  }

}
