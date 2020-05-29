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

package app.coronawarn.verification.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwTService implements Serializable {

  @Value("${jwt.secret}")
  private String secret;

  public static final String TOKEN_PREFIX = "Bearer ";

  public enum Roles {
    AUTH_C19_HOTLINE("c19hotline"),
    AUTH_C19_HEALTHAUTHORITY("c19healthauthority");

    private String roleName;

    Roles(final String role) {
      this.roleName = role;
    }

    String getRoleName() {
      return this.roleName;
    }

  }

  /**
   * Validates the given token. If one of the given roles {@link Roles} is.
   *
   * @param token The token to validate
   * @return <code>true</code>, if the token is valid, otherwise <code>false</code>
   */
  public boolean validateToken(final String token) {
    List<String> roleNames = getRoles(token);
    Roles[] roles = Roles.values();
    for (Roles role : roles) {
      if (roleNames.contains(role.getRoleName())) {
        return true;
      }
    }
    return false;
  }

  public String getSubject(final String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  private List<String> getRoles(final String token) {
    Map<String, List<String>> realm = getRealmFromToken(token);
    return realm.getOrDefault(ROLES, new ArrayList<>());
  }

  private static final String ROLES = "roles";

  private Map<String, List<String>> getRealmFromToken(final String token) {
    final Claims claims = getAllClaimsFromToken(token);
    Map<String, List<String>> realms = claims.get(REALM_ACCESS, Map.class);
    return realms;
  }

  private static final String REALM_ACCESS = "realm_access";

  public <T> T getClaimFromToken(final String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(final String token) {
    return Jwts.parser().setSigningKey(getSecret()).parseClaimsJws(token).getBody();
  }

  private byte[] getSecret() {
    try {
      return secret.getBytes("UTF-8");
    } catch (UnsupportedEncodingException ex) {
      log.warn(ex.getMessage(), ex);
    }

    return new byte[0];
  }

}
