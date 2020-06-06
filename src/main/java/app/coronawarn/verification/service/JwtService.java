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

import app.coronawarn.verification.client.IamClient;
import app.coronawarn.verification.model.AuthorizationRole;
import app.coronawarn.verification.model.Certs;
import app.coronawarn.verification.model.Key;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class represents the JWT service for token authorization and validation.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtService {

  /**
   * The prefix for the json web token.
   */
  public static final String TOKEN_PREFIX = "Bearer ";
  private static final String ROLES = "roles";
  private static final String REALM_ACCESS = "realm_access";

  @NonNull
  private final IamClient iamClient;

  /**
   * Validates the given token is given, the token starts with the needed prefix, 
   * the signing key is not null and the token is valid.
   *
   * @param authorizationToken The authorization token to validate
   * @return <code>true</code>, if the token is valid, otherwise
   * <code>false</code>
   */
  public boolean isAuthorized(String authorizationToken) {
    if (null != authorizationToken && authorizationToken.startsWith(JwtService.TOKEN_PREFIX)) {
      String jwtToken = authorizationToken.substring(JwtService.TOKEN_PREFIX.length());
      return validateToken(jwtToken, getPublicKey());
    }
    return false;
  }

  /**
   * Validates the given token. If one of the given roles
   * {@link AuthorizationRole} exists and verified by a public key
   *
   * @param token The authorization token to validate
   * @param publicKey the key from the IAM server
   * @return <code>true</code>, if the token is valid, otherwise
   * <code>false</code>
   */
  public boolean validateToken(final String token, final PublicKey publicKey) {
    if (null != publicKey) {
      try {
        List<String> roleNames = getRoles(token, publicKey);
        AuthorizationRole[] roles = AuthorizationRole.values();
        for (AuthorizationRole role : roles) {
          if (roleNames.contains(role.getRoleName())) {
            return true;
          }
        }
      } catch (JwtException ex) {
        log.warn("Token is not valid: {}.", ex.getMessage());
        return false;
      }
    }
    log.warn("No public key for Token validation found.");
    return false;
  }

  public String getSubject(final String token, final PublicKey publicKey) {
    return getClaimFromToken(token, Claims::getSubject, publicKey);
  }

  private List<String> getRoles(final String token, final PublicKey publicKey) {
    Map<String, List<String>> realm = getRealmFromToken(token, publicKey);
    return realm.getOrDefault(ROLES, new ArrayList<>());
  }

  private Map<String, List<String>> getRealmFromToken(final String token, final PublicKey publicKey) {
    final Claims claims = getAllClaimsFromToken(token, publicKey);
    Map<String, List<String>> realms = claims.get(REALM_ACCESS, Map.class);
    return realms;
  }

  public <T> T getClaimFromToken(final String token, Function<Claims, T> claimsResolver, final PublicKey publicKey) {
    final Claims claims = getAllClaimsFromToken(token, publicKey);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(final String token, final PublicKey publicKey) {
    Claims claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();
    return claims;
  }

  /**
   * Get the public key from IAM client.
   *
   * @return the calculated Public key from PEM
   */
  public PublicKey getPublicKey() {
    Certs certs = iamClient.certs();
    PublicKey pubKey = null;
    for (Key key : certs.getKeys()) {
      if (key.isCertValid()) {
        String certb64 = key.getX5c().get(0);
        try {
          KeyFactory kf = KeyFactory.getInstance("RSA");
          X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getMimeDecoder().decode(certb64));
          return kf.generatePublic(keySpecX509);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
          log.warn("Error getting public key: {}.", ex.getMessage());
        }
      } else {
        log.warn("Error getting public key - not the right use or alg key");
      }
    }
    return pubKey;
  }
}
