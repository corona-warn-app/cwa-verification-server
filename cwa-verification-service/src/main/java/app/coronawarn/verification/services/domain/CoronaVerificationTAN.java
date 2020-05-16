/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, A303220, T-Systems International GmbH
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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "TAN")
public class CoronaVerificationTAN implements Serializable {

    @Column(name = "ID", nullable = false, precision = 19)
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "TAN_HASH", columnDefinition = "VARCHAR2(64)")
    @Basic
    private String tanHash;

    @Column(name = "VALID_FROM", columnDefinition = "DATETIME(6)")
    @Basic
    private LocalDateTime validFrom;

    @Column(name = "VALID_UNTIL", columnDefinition = "DATETIME(6)")
    @Basic
    private LocalDateTime validUntil;

    @Column(name = "SOT", columnDefinition = "VARCHAR2(255)")
    @Basic
    private String sourceOfTrust;

    @Column(name = "REDEEMED", columnDefinition = "BIT")
    @Basic
    private boolean redeemed;

    @Column(name = "TYPE", columnDefinition = "VARCHAR2(255)")
    @Basic
    private String type;

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

    public String getTanHash() {
        return tanHash;
    }

    public void setTanHash(String tanHash) {
        this.tanHash = tanHash;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public String getSourceOfTrust() {
        return sourceOfTrust;
    }

    public void setSourceOfTrust(String sourceOfTrust) {
        this.sourceOfTrust = sourceOfTrust;
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public void setRedeemed(boolean redeemed) {
        this.redeemed = redeemed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.tanHash);
        hash = 53 * hash + Objects.hashCode(this.validFrom);
        hash = 53 * hash + Objects.hashCode(this.validUntil);
        hash = 53 * hash + Objects.hashCode(this.sourceOfTrust);
        hash = 53 * hash + (this.redeemed ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.createdOn);
        hash = 53 * hash + (int) (this.objVersion ^ (this.objVersion >>> 32));
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
        final CoronaVerificationTAN other = (CoronaVerificationTAN) obj;
        if (this.redeemed != other.redeemed) {
            return false;
        }
        if (!Objects.equals(this.tanHash, other.tanHash)) {
            return false;
        }
        if (!Objects.equals(this.sourceOfTrust, other.sourceOfTrust)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.validFrom, other.validFrom)) {
            return false;
        }
        if (!Objects.equals(this.validUntil, other.validUntil)) {
            return false;
        }
        return Objects.equals(this.createdOn, other.createdOn);
    }

    @Override
    public String toString() {
        return "CoronaVerificationTAN{" + "id=" + id + ", tanHash=" + tanHash
                + ", validFrom=" + validFrom + ", validUntil=" + validUntil
                + ", sourceOfTrust=" + sourceOfTrust + ", redeemed=" + redeemed
                + ", type=" + type + ", createdOn=" + createdOn
                + ", objVersion=" + objVersion + '}';
    }

}
