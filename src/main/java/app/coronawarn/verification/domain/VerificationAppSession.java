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

package app.coronawarn.verification.domain;

import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.TeleTanType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class represents the AppSession-entity.
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_session")
public class VerificationAppSession implements Serializable {

  private static final long serialVersionUID = 1L;

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

  @Column(name = "hashed_guid")
  private String hashedGuid;

  @Column(name = "hashed_guid_dob")
  private String hashedGuidDob;

  @Column(name = "registration_token_hash")
  private String registrationTokenHash;

  @Column(name = "tele_tan_hash")
  private String teleTanHash;

  @Column(name = "tan_counter")
  private int tanCounter;

  @Column(name = "sot")
  @Enumerated(EnumType.STRING)
  private AppSessionSourceOfTrust sourceOfTrust;

  @Column(name = "teletan_type")
  @Enumerated(EnumType.STRING)
  private TeleTanType teleTanType;

  /**
   * This method increments the tan counter.
   */
  public void incrementTanCounter() {
    this.tanCounter++;
  }

}
