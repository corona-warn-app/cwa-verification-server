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

package app.coronawarn.verification.factories.test.identifier;

import app.coronawarn.verification.exception.VerificationServerException;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.service.AppSessionService;
import app.coronawarn.verification.service.FakeDelayService;
import app.coronawarn.verification.service.TanService;
import java.util.concurrent.ScheduledExecutorService;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.async.DeferredResult;

@NoArgsConstructor
public class UnknownCovidTestIdentifier extends CovidTestIdentifier {

  @Override
  public DeferredResult<ResponseEntity<RegistrationToken>> generateRegistrationToken(
    RegistrationTokenRequest request,
    ScheduledExecutorService scheduledExecutor,
    StopWatch stopWatch,
    String fake,
    AppSessionService appSessionService,
    FakeDelayService fakeDelayService,
    TanService tanService) {

    stopWatch.stop();
    throw new VerificationServerException(HttpStatus.BAD_REQUEST,
      "Unknown registration key type for registration token");
  }
}
