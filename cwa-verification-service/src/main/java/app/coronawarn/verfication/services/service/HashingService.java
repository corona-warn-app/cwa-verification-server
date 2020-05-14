/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, A12248001, T-Systems International GmbH
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
package app.coronawarn.verfication.services.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 *
 * @author a12248001
 */
public class HashingService {
  private static final Logger LOG = LogManager.getLogger();

  /**
   * Returns the hash of the supplied string
   *
   * @param String that will be Hashed
   * @return the hash of the supplied string
   */
  public String hash(String toHash) {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Failed to use Hashfunction {}", e.getMessage());
    }
    byte[] hashed = digest.digest(
      toHash.getBytes(StandardCharsets.UTF_8));
    return  hashed.toString();
  }
}
