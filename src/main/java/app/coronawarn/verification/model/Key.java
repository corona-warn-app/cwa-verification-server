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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Key {
  public static final String SIG = "sig";
  public static final String RS256 = "RS256";
  private String kid;
  private String kty;
  private String alg;
  private String use;
  private String nn;
  private String ee;
  private List<String> x5c = null;
  private String x5t;
  private String x5tS256;
  private final Map<String, Object> additionalProperties = new HashMap<>();
  
  /**
   * Check if the cert is valid for use.
   * @return <code>true</code>, if the cert has the right use and alg keys, otherwise <code>false</code>
   */  
  
  public boolean isCertValid() {
    return getUse().equals(SIG) && getAlg().equals(RS256);
  }
}
