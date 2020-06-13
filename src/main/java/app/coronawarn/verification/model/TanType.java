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

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This class represents the different types of tans.
 *
 * @see <a href="https://github.com/corona-warn-app/cwa-verification-server/blob/master/docs/architecture-overview.md#entity-tan">Entity TAN - Type</a>
 */
@Schema
public enum TanType {
  /**
   * The general tan.
   */
  TAN,

  /**
   * The TeleTAN, which was created by, e.g the hotline.
   */
  TELETAN
}
