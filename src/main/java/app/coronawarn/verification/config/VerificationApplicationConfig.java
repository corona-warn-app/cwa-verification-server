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

package app.coronawarn.verification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This class and its nested subclasses are used to read in values from configuration file application.yml, which is
 * loaded via the '@EnableConfigurationProperties' annotation from SpringBootApplication main class.
 */
@Getter
@Setter
@ConfigurationProperties
public class VerificationApplicationConfig {
  private Long initialFakeDelayMilliseconds;

  private Long fakeDelayMovingAverageSamples;

  private Tan tan = new Tan();
  private AppSession appsession = new AppSession();
  private Entities entities = new Entities();
  private Jwt jwt = new Jwt();
  private Request request = new Request();

  private boolean disableDobHashCheckForExternalTestResult;

  /**
   * Configure the Tan with build property values and return the configured parameters.
   */
  @Getter
  @Setter
  public static class Tan {

    private Tele tele = new Tele();
    private Valid valid = new Valid();

    /**
     * Configure the Tele with build property values and return the configured parameters.
     */
    @Getter
    @Setter
    public static class Tele {

      private Valid valid = new Valid();
      private RateLimiting rateLimiting = new RateLimiting();

      /**
       * Configure the TeleValid with build property values and return the configured parameters.
       */
      @Getter
      @Setter
      public static class Valid {

        private String chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
        private int length = 1;
        // Number of hours that teleTAN remains valid
        private int hours = 1;
        private int eventDays = 2;
      }

      /**
       * Configure the rate limiting for creating new teletans.
       */
      @Getter
      @Setter
      public static class RateLimiting {

        // Number of seconds for the rate limiting time window
        private int seconds = 3600;
        // Number of teletans that are allowed to create within time window
        private int count = 1000;
        // Threshold in percent for a warning in log stream
        private int thresholdInPercent = 80;
      }
    }

    /**
     * Configure the Valid with build property values and return the configured parameters.
     */
    @Getter
    @Setter
    public static class Valid {

      // Number of days that TAN remains valid
      int days = 14;
    }
  }

  /**
   * Configure the AppSession with build property values and return the configured parameters.
   */
  @Getter
  @Setter
  public static class AppSession {

    // Maximum number of tans in a session at one time
    int tancountermax = 1;
  }

  /**
   * Configure the Entities with build property values and return the configured parameters.
   */
  @Getter
  @Setter
  public static class Entities {

    private Cleanup cleanup = new Cleanup();

    /**
     * Configure the Cleanup with build property values and return the configured parameters.
     */
    @Getter
    @Setter
    public static class Cleanup {

      private Integer days = 21;
    }

  }

  /**
   * Configure the Jwt with build property values and return the configured parameters.
   */
  @Getter
  @Setter
  public static class Jwt {

    private String server = "http://localhost:8080";
    private Boolean enabled = false;
  }

  /**
   * Configure the requests with build property values and return the configured parameters.
   */
  @Getter
  @Setter
  public static class Request {

    private long sizelimit = 10000;
  }
}
