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
  private long fakeDelay;

  public FakeDelayService(VerificationApplicationConfig applicationConfig) {
    this.fakeDelay = applicationConfig.getInitialFakeDelayMilliseconds();
    this.movingAverageSampleSize = applicationConfig.getFakeDelayMovingAverageSamples();
  }

  /**
   * Returns the current fake delay after applying random jitter.
   */
  public long getJitteredFakeDelay() {
    return new PoissonDistribution(fakeDelay).sample();
  }

  /**
   * Updates the moving average for the request duration with the specified value.
   */
  public void updateFakeRequestDelay(long realRequestDuration) {
    final long currentDelay = fakeDelay;
    fakeDelay = currentDelay + (realRequestDuration - currentDelay) / movingAverageSampleSize;
  }

  /**
   * Returns the current fake delay in seconds. Used for monitoring.
   */
  public Double getFakeDelayInSeconds() {
    return fakeDelay / 1000.;
  }
}
