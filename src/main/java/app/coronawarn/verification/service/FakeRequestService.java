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

package app.coronawarn.verification.service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import app.coronawarn.verification.model.LabTestResult;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.model.Tan;
import app.coronawarn.verification.model.TestResult;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.async.DeferredResult;


/**
 * This Service generates the fake responses for the Endpoints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FakeRequestService {

  @NonNull
  private final FakeDelayService fakeDelayService;

  private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(4);

  private static final Integer TEST_RESPONSE_PADDING_LENGTH = 45;
  private static final Integer TESTRESULT_RESULT_PADDING = 1;
  private static final Integer TAN_RESPONSE_PADDING_LENGTH = 15;


  /**
   * This method generates a fake transaction number by a Registration Token, if the state of the COVID-19 lab-test is
   * positive.
   *
   * @param registrationToken generated by a hashed guid or a teleTAN. {@link RegistrationToken}
   * @return A generated transaction number {@link Tan}.
   */
  public DeferredResult<ResponseEntity<Tan>> generateTan(@Valid @RequestBody RegistrationToken registrationToken) {
    long delay =  fakeDelayService.getJitteredFakeTanDelay();
    DeferredResult<ResponseEntity<Tan>> deferredResult = new DeferredResult<>();
    Tan returnTan = new Tan(UUID.randomUUID().toString(),
      RandomStringUtils.randomAlphanumeric(TAN_RESPONSE_PADDING_LENGTH));
    scheduledExecutor.schedule(() -> deferredResult.setResult(ResponseEntity.status(HttpStatus.CREATED)
      .body(returnTan)), delay, MILLISECONDS);
    return deferredResult;
  }


  /**
   * This method generates a fake registrationToken by a hashed guid or a teleTAN.
   *
   * @param request {@link RegistrationTokenRequest}
   * @return RegistrationToken - the created registration token {@link RegistrationToken}
   */
  public DeferredResult<ResponseEntity<RegistrationToken>> generateRegistrationToken(
    @RequestBody @Valid RegistrationTokenRequest request) {
    long delay =  fakeDelayService.getJitteredFakeTanDelay();
    DeferredResult<ResponseEntity<RegistrationToken>> deferredResult = new DeferredResult<>();
    scheduledExecutor.schedule(() -> deferredResult.setResult(ResponseEntity.status(HttpStatus.CREATED)
      .body(new RegistrationToken(UUID.randomUUID().toString(),
        RandomStringUtils.randomAlphanumeric(TESTRESULT_RESULT_PADDING)))), delay, MILLISECONDS);
    return deferredResult;
  }


  /**
   * Returns the fake test status of the COVID-19 test.
   *
   * @param registrationToken generated by a hashed guid {@link RegistrationToken}
   * @return the test result / status of the COVID-19 test, which can be POSITIVE, NEGATIVE, INVALID, PENDING or FAILED
   *     and will always be POSITIVE for a TeleTan
   */
  public DeferredResult<ResponseEntity<TestResult>> getTestState(
    @Valid @RequestBody RegistrationToken registrationToken) {
    long delay =  fakeDelayService.getJitteredFakeTanDelay();
    DeferredResult<ResponseEntity<TestResult>> deferredResult = new DeferredResult<>();
    scheduledExecutor.schedule(() -> deferredResult.setResult(ResponseEntity
      .ok(new TestResult(LabTestResult.POSITIVE.getTestResult(),
        RandomStringUtils.randomAlphanumeric(TEST_RESPONSE_PADDING_LENGTH)))), delay, MILLISECONDS);
    return deferredResult;
  }

}
