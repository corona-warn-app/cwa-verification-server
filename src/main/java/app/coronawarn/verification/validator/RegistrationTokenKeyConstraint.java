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

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * The validation-annotation for a registration token request.
 */
@Documented
@Constraint(validatedBy = RegistrationTokenRequestValidator.class)
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RegistrationTokenKeyConstraint {

  /**
   * The default key for creating error messages in case the constraint is violated.
   *
   * @return
   */
  String message() default "The key is not valid";

  /**
   * Allows the specification of validation groups, to which this constraint belongs.
   *
   * @return
   */
  Class<?>[] groups() default {};

  /**
   * Assigns custom payload objects to a constraint.
   *
   * @return
   */
  Class<? extends Payload>[] payload() default {};

}
