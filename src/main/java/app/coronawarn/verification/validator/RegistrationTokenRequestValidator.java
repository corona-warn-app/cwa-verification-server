/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, T-Systems International GmbH
 *
 * Deutsche Telekom AG, SAP AG and all other contributors /
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

package app.coronawarn.verification.validator;

import app.coronawarn.verification.model.RegistrationTokenKeyType;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.service.HashingService;
import app.coronawarn.verification.service.TanService;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * The registration token request validator.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RegistrationTokenRequestValidator
  implements ConstraintValidator<RegistrationTokenKeyConstraint, RegistrationTokenRequest> {
  /**
   * The {@link HashingService}.
   */
  @NonNull
  private final HashingService hashingService;

  /**
   * The {@link TanService}.
   */
  @NonNull
  private final TanService tanService;

  @Override
  public boolean isValid(RegistrationTokenRequest request, ConstraintValidatorContext arg1) {

    String key = request.getKey();
    RegistrationTokenKeyType keyType = request.getKeyType();
    if (key == null || keyType == null) {
      return false;
    }
    switch (keyType) {
      case GUID:
        return hashingService.isHashValid(key);
      case TELETAN: 
        return tanService.verifyTeleTan(key);
      default: 
        return false;
    }
  }
}
