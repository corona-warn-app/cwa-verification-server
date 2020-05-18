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
package app.coronawarn.verification.services.common;

import app.coronawarn.verification.services.common.TANKeyType;
import java.util.Objects;

/**
 * This class represents a tan request.
 */
public class TANRequest {

    /**
     * The key which can be a teletan or a regestration token.
     */
    private String key;

    /**
     * The type of key, which can be "token" or "teleTAN".
     */
    private TANKeyType keyType;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public TANKeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(TANKeyType tanType) {
        this.keyType = tanType;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.key);
        hash = 59 * hash + Objects.hashCode(this.keyType);
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
        final TANRequest other = (TANRequest) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return this.keyType == other.keyType;
    }

    @Override
    public String toString() {
        return "TANRequest{" + "key=" + key + ", tanType=" + keyType + '}';
    }
    
}
