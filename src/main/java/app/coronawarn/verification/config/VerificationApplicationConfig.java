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
 * This class and its subclasses are used to read in values from configuration file application.yml,
 * which is loaded via the '@EnableConfigurationProperties' annotation from SpringBootApplication main class.
 */
@Getter
@Setter
@ConfigurationProperties
public class VerificationApplicationConfig {

  private TanCfg tan;
  private AppSessionCfg appsession;
  private EntitiesCfg entities;

  public static class TeleCfg {
    @Getter
    @Setter
    private TeleValidCfg valid = new TeleValidCfg();

    public static class TeleValidCfg {
      @Getter
      @Setter
      private int hours = 1;
    }
  }

  public static class ValidCfg {
    @Getter
    @Setter
    private int days = 14;
  }

  public static class TanCfg {
    @Getter
    @Setter
    private TeleCfg tele = new TeleCfg();

    @Getter
    @Setter
    private ValidCfg valid = new ValidCfg();
  }

  public static class AppSessionCfg {
    @Getter
    @Setter
    private int tancountermax = 2;
  }

  public static class EntitiesCfg {
    @Getter
    @Setter
    private CleanupCfg cleanup = new CleanupCfg();
  }

  public static class CleanupCfg {
    @Getter
    @Setter
    private Integer days = 21;
  }
}
