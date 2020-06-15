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
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * This class represents the Exception Handler.
 */
@Slf4j
@RestControllerAdvice
public class VerificationExceptionHandler {

  /**
   * This method handles unknown Exceptions and Server Errors.
   *
   * @param ex the thrown exception
   * @param wr the WebRequest
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public void unknownException(Exception ex, WebRequest wr) {
    log.error("Unable to handle {}", wr.getDescription(false), ex);
  }

  /**
   * This method handles Bad Requests.
   *
   * @param ex the thrown exception
   * @param wr the WebRequest
   */
  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    ServletRequestBindingException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void bindingExceptions(Exception ex, WebRequest wr) {
    log.error("Binding failed {}", wr.getDescription(false), ex);
  }

  /**
   * This method handles Validation Exceptions.
   *
   * @return ResponseEntity<?> returns Bad Request
   */
  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    ConstraintViolationException.class
  })
  public ResponseEntity<?> handleValidationExceptions() {
    return ResponseEntity.badRequest().build();
  }

  /**
   * This method handles Validation Exceptions.
   *
   * @param exception the thrown exception
   * @return ResponseEntity<?> returns a HTTP Status
   */
  @ExceptionHandler(VerificationServerException.class)
  public ResponseEntity<Void> handleVerificationServerExceptions(VerificationServerException exception) {
    log.warn("The verification server response preventation due to: {}", exception.getMessage());
    return ResponseEntity.status(exception.getHttpStatus()).build();
  }

  /**
   * This method handles invalid HTTP methods.
   *
   * @param ex the thrown exception
   * @param wr the WebRequest
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public void methodNotAllowedException(Exception ex, WebRequest wr) {
    log.warn("Invalid http method {}", wr.getDescription(false), ex);
  }

  /**
   * This method handles unsupported content types.
   *
   * @param ex the thrown exception
   * @param wr the WebRequest
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  public void contentTypeNotAllowedException(Exception ex, WebRequest wr) {
    log.warn("Unsupported content type {}", wr.getDescription(false), ex);
  }
}
