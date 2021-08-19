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
import app.coronawarn.verification.config.VerificationApplicationConfig;
import app.coronawarn.verification.model.AuthorizationRole;
import app.coronawarn.verification.model.Certs;
import app.coronawarn.verification.model.Key;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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
   * The bearer prefix for the json web token.
   */
  public static final String TOKEN_PREFIX = "Bearer ";
  /**
   * The certificate begin prefix.
   */
  public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
  /**
   * The certificate end suffix.
   */
  public static final String END_CERT = "-----END CERTIFICATE-----";
  /**
   * The http request header name for JWT 'Authorization'.
   */
  public static final String HEADER_NAME_AUTHORIZATION = "Authorization";

  private static final String ROLES = "roles";
  private static final String REALM_ACCESS = "realm_access";

  @NonNull
  private final IamClient iamClient;

  @NonNull
  private final VerificationApplicationConfig verificationApplicationConfig;

  /**
   * Validates the given token is given, the token starts with the needed prefix, the signing key is not null and the
   * token is valid.
   *
   * @param authorizationToken The authorization token to validate
   * @param mandatoryRoles list of roles which are required to pass
   * @return <code>true</code>, if the token is valid, otherwise <code>false</code>
   */
  public boolean isAuthorized(String authorizationToken, List<AuthorizationRole> mandatoryRoles) {
    // check if the JWT is enabled
    if (!verificationApplicationConfig.getJwt().getEnabled()) {
      return true;
    }
    if (null != authorizationToken && authorizationToken.startsWith(TOKEN_PREFIX)) {
      String jwtToken = authorizationToken.substring(TOKEN_PREFIX.length());
      return validateToken(jwtToken, getPublicKey(), mandatoryRoles);
    }
    return false;
  }

  /**
   * Validates the given token. If one of the given roles {@link AuthorizationRole} exists and verified by a public key
   *
   * @param token The authorization token to validate
   * @param publicKey the key from the IAM server
   * @param mandatoryRoles List of roles which are required to pass.
   * @return <code>true</code>, if the token is valid, otherwise <code>false</code>
   */
  public boolean validateToken(final String token, final PublicKey publicKey, List<AuthorizationRole> mandatoryRoles) {
    log.debug("process validateToken() by - token: {} PK: {}", token, publicKey);
    if (null != publicKey) {
      try {
        List<String> roleNames = getRoles(token, publicKey);

        // Return false if one of the mandatory roles are not present
        for (AuthorizationRole mandatoryRole : mandatoryRoles) {
          if (!roleNames.contains(mandatoryRole.getRoleName())) {
            return false;
          }
        }

        // Return true if at least one of the authorization roles are present
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

  @SuppressWarnings("unchecked")
  private Map<String, List<String>> getRealmFromToken(final String token, final PublicKey publicKey) {
    final Claims claims = getAllClaimsFromToken(token, publicKey);
    return claims.get(REALM_ACCESS, Map.class);
  }

  public <T> T getClaimFromToken(final String token, Function<Claims, T> claimsResolver, final PublicKey publicKey) {
    final Claims claims = getAllClaimsFromToken(token, publicKey);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(final String token, final PublicKey publicKey) {
    return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
  }

  /**
   * Get the certificate from IAM client.
   * As long as Keycloak can rotate it’s keys we decided to reload 
   * the key on every validateToken call especially the method call 
   * is fortunately limited in time and number too.
   *
   * @return the calculated Public key from the certificate
   */
  public PublicKey getPublicKey() {
    Certs certs = iamClient.certs();
    log.debug("process getPublicKey() - cert info from IAM certs: {}", certs);
    for (Key key : certs.getKeys()) {
      if (key.isCertValid()) {
        String certb64 = key.getX5c().get(0);
        String wrappedCert = BEGIN_CERT + System.lineSeparator() + certb64 + System.lineSeparator() + END_CERT;
        try {
          byte[] certBytes = wrappedCert.getBytes(java.nio.charset.StandardCharsets.UTF_8);
          CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
          InputStream in = new ByteArrayInputStream(certBytes);
          X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(in);
          return certificate.getPublicKey();
        } catch (CertificateException ex) {
          log.warn("Error generate certificate: {}.", ex.getMessage());
        }
      } else {
        log.warn("Wrong use or alg key given! use: {} alg: {}", key.getUse(), key.getAlg());
        log.warn("Keys use: {} and alg: {} are expected!", Key.SIG, Key.RS256);
      }
    }
    return null;
  }
}
