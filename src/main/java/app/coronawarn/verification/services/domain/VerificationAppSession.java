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
package app.coronawarn.verification.services.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * This class represents the AppSession-entity.
 */
@Entity
@Table(name = "APP_SESSION")
public class VerificationAppSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ID", nullable = false, precision = 19)
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "GUID_HASH", columnDefinition = "VARCHAR2(64)")
    @Basic
    private String guidHash;

    @Column(name = "REGISTRATION_TOKEN_HASH", columnDefinition = "VARCHAR2(64)")
    @Basic
    private String registrationTokenHash;

    @Column(name = "TELE_TAN_HASH", columnDefinition = "VARCHAR2(64)")
    @Basic
    private String teleTanHash;

    @Column(name = "TAN_COUNTER", columnDefinition = "INT")
    @Basic
    private String tanCounter;

    @Column(name = "SOT", columnDefinition = "VARCHAR(255)")
    @Basic
    private String sourceOfTrust;

    @Column(name = "TAN_GENERATED_FLAG", columnDefinition = "BIT")
    @Basic
    private boolean tanGenerated;

    @Column(name = "CREATED_ON", columnDefinition = "DATETIME(6)")
    @Basic
    private LocalDateTime createdOn;

    @Column(name = "OBJ_VERSION", columnDefinition = "BIGINT")
    @Version
    private long objVersion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuidHash() {
        return guidHash;
    }

    public void setGuidHash(String guidHash) {
        this.guidHash = guidHash;
    }

    public String getRegistrationTokenHash() {
        return registrationTokenHash;
    }

    public void setRegistrationTokenHash(String registrationTokenHash) {
        this.registrationTokenHash = registrationTokenHash;
    }

    public boolean isTanGenerated() {
        return tanGenerated;
    }

    public void setTanGenerated(boolean tanGenerated) {
        this.tanGenerated = tanGenerated;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getTanCounter() {
        return tanCounter;
    }

    public void setTanCounter(String tanCounter) {
        this.tanCounter = tanCounter;
    }

    public String getSourceOfTrust() {
        return sourceOfTrust;
    }

    public void setSourceOfTrust(String sourceOfTrust) {
        this.sourceOfTrust = sourceOfTrust;
    }

    public String getTeleTanHash() {
        return teleTanHash;
    }

    public void setTeleTanHash(String teleTanHash) {
        this.teleTanHash = teleTanHash;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.id);
        hash = 29 * hash + Objects.hashCode(this.guidHash);
        hash = 29 * hash + Objects.hashCode(this.registrationTokenHash);
        hash = 29 * hash + Objects.hashCode(this.teleTanHash);
        hash = 29 * hash + Objects.hashCode(this.tanCounter);
        hash = 29 * hash + Objects.hashCode(this.sourceOfTrust);
        hash = 29 * hash + (this.tanGenerated ? 1 : 0);
        hash = 29 * hash + Objects.hashCode(this.createdOn);
        hash = 29 * hash + (int) (this.objVersion ^ (this.objVersion >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VerificationAppSession other = (VerificationAppSession) obj;
        if (this.tanGenerated != other.tanGenerated) {
            return false;
        }
        if (!Objects.equals(this.guidHash, other.guidHash)) {
            return false;
        }
        if (!Objects.equals(this.registrationTokenHash, other.registrationTokenHash)) {
            return false;
        }
        if (!Objects.equals(this.teleTanHash, other.teleTanHash)) {
            return false;
        }
        if (!Objects.equals(this.tanCounter, other.tanCounter)) {
            return false;
        }
        if (!Objects.equals(this.sourceOfTrust, other.sourceOfTrust)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.createdOn, other.createdOn);
    }

    @Override
    public String toString() {
        return "VerificationAppSession{" + "id=" + id + ", guidHash=" + guidHash
                + ", registrationTokenHash=" + registrationTokenHash
                + ", teleTanHash=" + teleTanHash + ", tanCounter=" + tanCounter
                + ", sourceOfTrust=" + sourceOfTrust + ", tanGenerated=" + tanGenerated
                + ", createdOn=" + createdOn + ", objVersion=" + objVersion + '}';
    }

}
