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
package app.coronawarn.verification.services.service;

import app.coronawarn.verification.services.common.RegistrationTokenKeyType;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the hashing service.
 */
@Component
public class HashingService {
    private static final String GUID_HASH_PATTERN = "[0-9A-Fa-f]{64}";
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Returns the hash of the supplied string
     *
     * @param toHash that will be Hashed
     * @return the hash of the supplied string
     */
    public String hash(String toHash) {
        LOG.info("HashingService - hash has been called.");
        return DigestUtils.sha256Hex(toHash);
    }

    /**
     * Returns true if the String is resembles a SHA256 Pattern
     *
     * @param toValidate String that will be checked to match the pattern of a SHA256 Hash
     * @return Boolean if the String Matches the Pattern
     */
    public boolean isHashValid(String toValidate) {
        Pattern pattern = Pattern.compile(GUID_HASH_PATTERN);
        Matcher matcher = pattern.matcher(toValidate);
        if( matcher.find()) {
            return true;
        }
        return false;
    }
}
