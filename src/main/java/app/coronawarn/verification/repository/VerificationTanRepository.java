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

package app.coronawarn.verification.repository;

import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.model.TanType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * This class represents the Tan repository.
 */
public interface VerificationTanRepository extends JpaRepository<VerificationTan, Long> {

  /**
   * This method looks in the Database for an if a VerificationTan exists for the tan hash.
   *
   * @param tanHash hash to search for
   * @return Boolean if there is an Entity for the tanHash
   */
  boolean existsByTanHash(String tanHash);

  /**
   * This method looks in the Database for an if a VerificationTan exists for the tan hash.
   *
   * @param tanHash hash to search for
   * @return Optional VerificationTan
   */
  Optional<VerificationTan> findByTanHash(String tanHash);

  /**
   * This method purges Entities from the database that are older than before value.
   *
   * @param before LocalDateTime to delete older entities
   */
  void deleteByCreatedAtBefore(LocalDateTime before);

  /**
   * This method counts entities which are newer then after value.
   *
   * @param after - LocalDateTime to count entities
   * @param tanType - TanType of the tans that should be counted
   * @return number of relevant entities
   */
  int countByCreatedAtIsAfterAndTypeIs(LocalDateTime after, TanType tanType);

}
