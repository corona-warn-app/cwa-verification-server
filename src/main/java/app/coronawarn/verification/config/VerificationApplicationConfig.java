/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, YOUR_NAME, YOUR_COMPANY
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
 * This class is used to read in values from configuration file application.yml.
 * It is loaded via the @EnableConfigurationProperties annotation from SpringBootApplication main class. */
@ConfigurationProperties
public class VerificationApplicationConfig {

  public static class TeleCfg {
    public static class TeleValidCfg {
      /// Lifespan of TeleTAN in hours
      @Getter @Setter private int hours = 1;
    }

    @Getter @Setter private TeleValidCfg valid = new TeleValidCfg();
  }

  public static class ValidCfg {
    /// Lifespan of TAN in days
    @Getter @Setter int days = 14;
  }

  public static class TanCfg {
    @Getter @Setter private TeleCfg tele = new TeleCfg();
    @Getter @Setter private ValidCfg valid = new ValidCfg();
  }

  public static class AppSessionCfg {
    /// maximum number of TANs generated for a session
    @Getter @Setter int tancountermax = 2;
  }
  
  @Getter @Setter private TanCfg tan = new TanCfg();
  @Getter @Setter private AppSessionCfg appsession = new AppSessionCfg();

}
