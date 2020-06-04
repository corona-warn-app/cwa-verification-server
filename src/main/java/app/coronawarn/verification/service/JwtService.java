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

import static org.apache.commons.codec.binary.Base64.decodeBase64;

import app.coronawarn.verification.config.VerificationApplicationConfig;
import app.coronawarn.verification.model.AuthorizationRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.ws.rs.client.Client;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;





/**
 * This class represents the JWT service for token validation.
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
  private static final String RESPONSE_PUBLICKEY_VALUE = "value";
  protected Client client;
  @NonNull
  private final VerificationApplicationConfig verificationApplicationConfig;

  /**
   * Validates the given token. If one of the given roles
   * {@link AuthorizationRole} exists.
   *
   * @param authorizationToken The authorization token to validate
   * @return <code>true</code>, if the token is valid, otherwise
   * <code>false</code>
   */
  public boolean isAuthorized(String authorizationToken) {
    if (null != authorizationToken && authorizationToken.startsWith(JwtService.TOKEN_PREFIX)) {
      String requestToken = authorizationToken.substring(JwtService.TOKEN_PREFIX.length());
      return validateToken(requestToken);
    }
    return false;
  }

  private boolean validateToken(final String token) {
    try {
      List<String> roleNames = getRoles(token);
      AuthorizationRole[] roles = AuthorizationRole.values();
      for (AuthorizationRole role : roles) {
        if (roleNames.contains(role.getRoleName())) {
          return true;
        }
      }
    } catch (JwtException ex) {
      log.warn("Token is not valid.");
      return false;
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

  private Map<String, List<String>> getRealmFromToken(final String token) {
    final Claims claims = getAllClaimsFromToken(token);
    Map<String, List<String>> realms = claims.get(REALM_ACCESS, Map.class);
    return realms;
  }

  public <T> T getClaimFromToken(final String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(final String token) {
    return Jwts.parser().setSigningKey(getSecret()).parseClaimsJws(token).getBody();
  }

  private byte[] getSecret() {
    try {
      String secret = verificationApplicationConfig.getJwt().getSecret();
      return secret.getBytes("UTF-8");
    } catch (UnsupportedEncodingException ex) {
      log.warn(ex.getMessage(), ex);
    }
    return new byte[0];
  }

  private PublicKey getPublicKey(String keyUrl) {

    if (keyUrl != null && !keyUrl.isEmpty()) {
      try {
        Map<String, Object> publicKey = client.target(keyUrl).request().get(Map.class);
        String ssoPublicKey = publicKey.get(RESPONSE_PUBLICKEY_VALUE).toString().split("----\n")[1].split("\n---")[0];
        byte[] publicBytes = decodeBase64(ssoPublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        return pubKey;
      } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
        throw new IllegalArgumentException("error getting public key");
      }
    }

    return null;
  }

}
