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

package app.coronawarn.verification.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A filter to avoid requests with a large content and chunked requests.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PostSizeLimitFilter extends OncePerRequestFilter {

  @NonNull
  private final VerificationApplicationConfig verificationApplicationConfig;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
    HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    long maxPostSize = verificationApplicationConfig.getRequest().getSizelimit();
    if (isPost(request) && (request.getContentLengthLong() > maxPostSize || request.getContentLengthLong() == -1)) {
      log.warn("The request size is too large or the request was sent via chunks.");
      response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean isPost(HttpServletRequest httpRequest) {
    return HttpMethod.POST.matches(httpRequest.getMethod());
  }

}
