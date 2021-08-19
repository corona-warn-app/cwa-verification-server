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

package app.coronawarn.verification.domain;

import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.model.TanType;
import app.coronawarn.verification.model.TeleTanType;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class represents the TAN - entity.
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tan")
public class VerificationTan implements Serializable {

  static final long SERIAL_VERSION_UID = 1L;
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Version
  @Column(name = "version")
  private long version;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "tan_hash")
  private String tanHash;

  @Column(name = "valid_from")
  private LocalDateTime validFrom;

  @Column(name = "valid_until")
  private LocalDateTime validUntil;

  @Column(name = "sot")
  @Enumerated(EnumType.STRING)
  private TanSourceOfTrust sourceOfTrust;

  @Column(name = "redeemed")
  private boolean redeemed;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private TanType type;

  @Column(name = "teletan_type")
  @Enumerated(EnumType.STRING)
  private TeleTanType teleTanType;

  /**
   * Check if the tan can be redeemed by date.
   *
   * @param reference the date to check if it is in between from and until range
   * @return true or false if it can be redeemed
   */
  public boolean canBeRedeemed(LocalDateTime reference) {
    return validFrom.isBefore(reference)
      && validUntil.isAfter(reference)
      && !isRedeemed();
  }

}
