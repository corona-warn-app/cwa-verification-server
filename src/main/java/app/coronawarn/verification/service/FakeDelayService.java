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

import app.coronawarn.verification.config.VerificationApplicationConfig;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.springframework.stereotype.Component;

/**
 * {@link  FakeDelayService} instances manage the response delay in the processing of fake (or "dummy") requests.
 */
@Component
public class FakeDelayService {

  private final long movingAverageSampleSize;
  private long fakeDelayTest;

  private long fakeDelayTan;

  private long fakeDelayToken;

  /**
   * Constructor for the FakeDelayService.
   */
  public FakeDelayService(VerificationApplicationConfig applicationConfig) {
    this.fakeDelayTest = applicationConfig.getInitialFakeDelayMilliseconds();
    this.fakeDelayTan = applicationConfig.getInitialFakeDelayMilliseconds();
    this.fakeDelayToken = applicationConfig.getInitialFakeDelayMilliseconds();
    this.movingAverageSampleSize = applicationConfig.getFakeDelayMovingAverageSamples();
  }

  /**
   * Returns the current fake delay after applying random jitter.
   */
  public long getJitteredFakeTanDelay() {
    return new PoissonDistribution(fakeDelayTan).sample();
  }

  /**
   * Returns the current fake delay after applying random jitter.
   */
  public long getJitteredFakeTestDelay() {
    return new PoissonDistribution(fakeDelayTest).sample();
  }

  /**
   * Returns the current fake delay after applying random jitter.
   */
  public long getJitteredFakeTokenDelay() {
    return new PoissonDistribution(fakeDelayToken).sample();
  }

  /**
   * Updates the moving average for the request duration for the Tan Endpoint with the specified value.
   */
  public void updateFakeTanRequestDelay(long realRequestDuration) {
    final long currentDelay = fakeDelayTan;
    fakeDelayTan = currentDelay + (realRequestDuration - currentDelay) / movingAverageSampleSize;
  }

  /**
   * Updates the moving average for the request duration for the Tan Endpoint with the specified value.
   */
  public void updateFakeTestRequestDelay(long realRequestDuration) {
    final long currentDelay = fakeDelayTest;
    fakeDelayTan = currentDelay + (realRequestDuration - currentDelay) / movingAverageSampleSize;
  }

  /**
   * Updates the moving average for the request duration for the Tan Endpoint with the specified value.
   */
  public void updateFakeTokenRequestDelay(long realRequestDuration) {
    final long currentDelay = fakeDelayToken;
    fakeDelayTan = currentDelay + (realRequestDuration - currentDelay) / movingAverageSampleSize;
  }

  /**
   * Returns the current fake delay in seconds. Used for monitoring.
   */
  public Double getFakeTanDelayInSeconds() {
    return fakeDelayTan / 1000.;
  }

  /**
   * Returns the current fake delay in seconds. Used for monitoring.
   */
  public Double getFakeTestDelayInSeconds() {
    return fakeDelayTest / 1000.;
  }

  /**
   * Returns the current fake delay in seconds. Used for monitoring.
   */
  public Double getFakeTokenDelayInSeconds() {
    return fakeDelayToken / 1000.;
  }

  /**
   * Returns the longest fake delay jittered in milliseconds.
   * @return longest jittered
   */
  public long getLongestJitter() {
    if ((fakeDelayTan > fakeDelayTest) && (fakeDelayTan > fakeDelayToken)) {
      return getJitteredFakeTanDelay();
    } else if ((fakeDelayToken > fakeDelayTest) && (fakeDelayToken > fakeDelayTan)) {
      return getJitteredFakeTokenDelay();
    } else {
      return getJitteredFakeTestDelay();
    }
  }

  /**
   * Returns the longest fake delay minus average time for Tan in milliseconds.
   * @return delay for TAN
   */
  public long realDelayTan() {
    return (getLongestJitter() - getJitteredFakeTanDelay());
  }

  /**
   * Returns the longest fake delay minus average time for RegistrationToken in milliseconds.
   * @return delay for RegistrationToken
   */
  public long realDelayToken() {
    return (getLongestJitter() - getJitteredFakeTokenDelay());
  }

  /**
   * Returns the longest fake delay minus average time for TestResult in milliseconds.
   * @return delay for TestResult
   */
  public long realDelayTest() {
    return (getLongestJitter() - getJitteredFakeTestDelay());
  }
}
