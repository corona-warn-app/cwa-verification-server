/*-
 * ---license-start
 * Corona-Warn-App / cwa-verification
 * ---
 * Copyright (C) 2020 - 2022 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.verification.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A filter to avoid requests with a large content and chunked requests.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestSizeLimitFilter extends OncePerRequestFilter {

  private final VerificationApplicationConfig verificationApplicationConfig;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
    throws ServletException, IOException {
    long maxPostSize = verificationApplicationConfig.getRequest().getSizelimit();
    if (request.getContentLengthLong() > maxPostSize || isChunkedRequest(request)) {
      log.warn("The request size is too large or the request was sent via chunks.");
      response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean isChunkedRequest(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.TRANSFER_ENCODING);

    return !StringUtils.isEmpty(header) && header.equalsIgnoreCase("chunked");
  }

}
