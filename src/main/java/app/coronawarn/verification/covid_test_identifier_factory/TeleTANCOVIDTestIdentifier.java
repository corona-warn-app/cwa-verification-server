/*-
 * ---license-start
 * Corona-Warn-App / cwa-verification
 * ---
 * Copyright (C) 2020 - 2022 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.verification.covid_test_identifier_factory;

import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.exception.VerificationServerException;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.service.AppSessionService;
import app.coronawarn.verification.service.FakeDelayService;
import app.coronawarn.verification.service.TanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RequiredArgsConstructor
@Slf4j
public class TeleTANCOVIDTestIdentifier extends COVIDTestIdentifier {

  private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(4);

  @Override
  public DeferredResult<ResponseEntity<RegistrationToken>> generateRegistrationToken(RegistrationTokenRequest request, ScheduledExecutorService scheduledExecutor, StopWatch stopWatch, String fake, AppSessionService appSessionService, FakeDelayService fakeDelayService, TanService tanService) {
    Optional<VerificationTan> optional = tanService.getEntityByTan(request.getKey());

    ResponseEntity<RegistrationToken> response = appSessionService.generateRegistrationTokenByTeleTan(
      request.getKey(),
      fake,
      optional.map(VerificationTan::getTeleTanType).orElse(null));

    if (optional.isPresent()) {
      VerificationTan teleTan = optional.get();
      teleTan.setRedeemed(true);
      tanService.saveTan(teleTan);
      stopWatch.stop();
      fakeDelayService.updateFakeTokenRequestDelay(stopWatch.getTotalTimeMillis());
      DeferredResult<ResponseEntity<RegistrationToken>> deferredResult = new DeferredResult<>();
      scheduledExecutor.schedule(() -> deferredResult.setResult(response), fakeDelayService.realDelayToken(),
        MILLISECONDS);
      log.info("Returning the successfully generated RegistrationToken.");
      return deferredResult;
    }
    stopWatch.stop();
    throw new VerificationServerException(HttpStatus.BAD_REQUEST, "The teleTAN verification failed");
  }
}
