package app.coronawarn.verification.controller;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.exception.VerificationServerException;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.RegistrationTokenKeyType;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.service.AppSessionService;
import app.coronawarn.verification.service.FakeDelayService;
import app.coronawarn.verification.service.FakeRequestService;
import app.coronawarn.verification.service.TanService;
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
 * This class represents the rest controller for externally reachable TAN interactions.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/version/v1")
@Validated
@Profile("external")
public class ExternalTokenController {

  /**
   * The route to the token registration endpoint.
   */
  public static final String REGISTRATION_TOKEN_ROUTE = "/registrationToken";

  private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(4);

  private final FakeRequestService fakeRequestController;

  private final AppSessionService appSessionService;

  private final TanService tanService;

  private final FakeDelayService fakeDelayService;

  /**
   * This method generates a registrationToken by a hashed guid or a teleTAN.
   *
   * @param request {@link RegistrationTokenRequest}
   * @param fake    flag for fake request
   * @return RegistrationToken - the created registration token {@link RegistrationToken}
   */
  @Operation(
    summary = "Get registration Token",
    description = "Get a registration token by providing a SHA-256 hasehd GUID or a teleTAN")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "registration token generated."),
    @ApiResponse(responseCode = "400", description = "GUID/TeleTAN already exists.")})
  @PostMapping(value = REGISTRATION_TOKEN_ROUTE,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
  public DeferredResult<ResponseEntity<RegistrationToken>> generateRegistrationToken(
    @RequestBody @Valid RegistrationTokenRequest request,
    @RequestHeader(value = "cwa-fake", required = false) String fake) {
    if ((fake != null) && (fake.equals("1"))) {
      return fakeRequestController.generateRegistrationToken(request);
    }
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    String key = request.getKey();
    RegistrationTokenKeyType keyType = request.getKeyType();
    DeferredResult<ResponseEntity<RegistrationToken>> deferredResult = new DeferredResult<>();

    switch (keyType) {
      case GUID:
        ResponseEntity<RegistrationToken> responseEntity =
          appSessionService.generateRegistrationTokenByGuid(key, request.getKeyDob(), fake);
        stopWatch.stop();
        fakeDelayService.updateFakeTokenRequestDelay(stopWatch.getTotalTimeMillis());
        deferredResult.setResult(responseEntity);
        log.info("Returning the successfully generated RegistrationToken.");
        return deferredResult;
      case TELETAN:
        Optional<VerificationTan> optional = tanService.getEntityByTan(key);

        ResponseEntity<RegistrationToken> response = appSessionService.generateRegistrationTokenByTeleTan(
          key,
          fake,
          optional.map(VerificationTan::getTeleTanType).orElse(null));

        if (optional.isPresent()) {
          VerificationTan teleTan = optional.get();
          teleTan.setRedeemed(true);
          tanService.saveTan(teleTan);
          stopWatch.stop();
          fakeDelayService.updateFakeTokenRequestDelay(stopWatch.getTotalTimeMillis());
          scheduledExecutor.schedule(() -> deferredResult.setResult(response), fakeDelayService.realDelayToken(),
            MILLISECONDS);
          log.info("Returning the successfully generated RegistrationToken.");
          return deferredResult;
        }
        stopWatch.stop();
        throw new VerificationServerException(HttpStatus.BAD_REQUEST, "The teleTAN verification failed");
      default:
        stopWatch.stop();
        throw new VerificationServerException(HttpStatus.BAD_REQUEST,
          "Unknown registration key type for registration token");
    }
  }
}
