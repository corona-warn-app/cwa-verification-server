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

package app.coronawarn.verification.model;

import app.coronawarn.verification.validator.RegistrationTokenKeyConstraint;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents a registration token request parameter with a hashed guid or a teleTAN.
 */
@Schema(
  description = "The registration token request model."
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegistrationTokenKeyConstraint
public class RegistrationTokenRequest {

  /**
   * The key which can be a teletan or a hashed guid.
   */
  @NotNull
  private String key;

  /**
   * The hashed GUID built with date of birth.
   */
  @Pattern(regexp = "^[XxA-Fa-f0-9]([A-Fa-f0-9]{63})$")
  private String keyDob;

  /**
   * The type of key, which can be "GUID" or "TELETAN".
   */
  @NotNull
  private RegistrationTokenKeyType keyType;
}
