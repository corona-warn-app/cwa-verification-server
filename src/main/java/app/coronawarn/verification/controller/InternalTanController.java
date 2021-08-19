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

package app.coronawarn.verification.controller;

import app.coronawarn.verification.exception.VerificationServerException;
import app.coronawarn.verification.model.AuthorizationRole;
import app.coronawarn.verification.model.AuthorizationToken;
import app.coronawarn.verification.model.Tan;
import app.coronawarn.verification.model.TeleTan;
import app.coronawarn.verification.model.TeleTanType;
import app.coronawarn.verification.service.JwtService;
import app.coronawarn.verification.service.TanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class represents the rest controller for internally needed tan operations.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/version/v1")
@Validated
@Profile("internal")
public class InternalTanController {

  /**
   * The route to the tan verification endpoint.
   */
  public static final String TAN_VERIFY_ROUTE = "/tan/verify";
  /**
   * The route to the teleTAN generation endpoint.
   */
  public static final String TELE_TAN_ROUTE = "/tan/teletan";

  public static final String TELE_TAN_TYPE_HEADER = "X-CWA-TELETAN-TYPE";

  @NonNull
  private final TanService tanService;

  @NonNull
  private final JwtService jwtService;

  /**
   * This provided REST method verifies the transaction number (TAN).
   *
   * @param tan - the transaction number, which needs to be verified {@link Tan}
   * @return HTTP 200, if the verification was successful. Otherwise HTTP 404.
   */
  @Operation(
    summary = "Verify provided Tan",
    description = "The provided Tan is verified to be formerly issued by the verification server"
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Tan is valid an formerly issued by the verification server",
      headers = {
        @Header(name = TELE_TAN_TYPE_HEADER, description = "Type of the TeleTan (TEST or EVENT)")
      }),
    @ApiResponse(responseCode = "404", description = "Tan could not be verified")})
  @PostMapping(value = TAN_VERIFY_ROUTE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<?> verifyTan(@Valid @RequestBody Tan tan) {
    return tanService.getEntityByTan(tan.getTan())
      .filter(t -> t.canBeRedeemed(LocalDateTime.now()))
      .map(t -> {
        tanService.deleteTan(t);
        log.info("The Tan is valid.");
        return t;
      })
      .map(t -> {
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();

        if (t.getTeleTanType() != null) {
          responseBuilder.header(TELE_TAN_TYPE_HEADER, t.getTeleTanType().toString());
        }

        return responseBuilder.build();
      })
      .orElseGet(() -> {
        log.info("The Tan is invalid.");
        throw new VerificationServerException(HttpStatus.NOT_FOUND, "No Tan found or Tan is invalid");
      });
  }

  /**
   * This method generates a valid teleTAN.
   *
   * @param authorization auth
   * @return a created teletan
   */
  @Operation(
    summary = "Request generation of a teleTAN",
    description = "A teleTAN is a human readable TAN with 7 characters which is supposed to be issued via call line"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "TeleTan created")})
  @PostMapping(value = TELE_TAN_ROUTE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<TeleTan> createTeleTan(
    @RequestHeader(JwtService.HEADER_NAME_AUTHORIZATION) @Valid AuthorizationToken authorization,
    @RequestHeader(value = TELE_TAN_TYPE_HEADER, required = false) @Valid TeleTanType teleTanType) {

    List<AuthorizationRole> requiredRoles = new ArrayList<>();

    if (teleTanType == null) {
      teleTanType = TeleTanType.TEST;
      requiredRoles.add(AuthorizationRole.AUTH_C19_HOTLINE);
    } else if (teleTanType == TeleTanType.EVENT) {
      requiredRoles.add(AuthorizationRole.AUTH_C19_HOTLINE_EVENT);
    }

    if (jwtService.isAuthorized(authorization.getToken(), requiredRoles)) {
      if (tanService.isTeleTanRateLimitNotExceeded()) {
        String teleTan = tanService.generateVerificationTeleTan(teleTanType);
        log.info("The teleTAN is generated.");
        return ResponseEntity.status(HttpStatus.CREATED).body(new TeleTan(teleTan));
      } else {
        throw new VerificationServerException(HttpStatus.TOO_MANY_REQUESTS, "Rate Limit exceed. Try again later.");
      }
    }
    throw new VerificationServerException(HttpStatus.UNAUTHORIZED, "JWT is invalid.");
  }

}
