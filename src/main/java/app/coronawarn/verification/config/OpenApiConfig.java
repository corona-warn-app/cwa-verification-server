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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class represents the open api config.
 */
@RequiredArgsConstructor
@Configuration
public class OpenApiConfig {

  private final BuildProperties buildProperties;

  /**
   * Configure the open api bean with build property values.
   *
   * @return the configured open api config
   */
  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
      .info(new Info()
        .title(buildProperties.getArtifact())
        .version(buildProperties.getVersion())
        .license(new License()
          .name("Apache 2.0")
          .url("http://www.apache.org/licenses/LICENSE-2.0")));
  }
}
