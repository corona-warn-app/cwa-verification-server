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

package app.coronawarn.verification.validator;

import app.coronawarn.verification.model.RegistrationTokenKeyType;
import app.coronawarn.verification.model.RegistrationTokenRequest;
import app.coronawarn.verification.service.HashingService;
import app.coronawarn.verification.service.TanService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
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
    return switch (keyType) {
      case GUID -> hashingService.isHashValid(key);
      case TELETAN -> tanService.verifyTeleTan(key);
    };
  }
}
