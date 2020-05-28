package app.coronawarn.verification.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PostSizeLimitFilter extends OncePerRequestFilter {

  @Value("${server.max-post-size:10000}")
  private long maxPostSize;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    if (isPost(request) && request.getContentLengthLong() > maxPostSize) {
      response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean isPost(HttpServletRequest httpRequest) {
    return HttpMethod.POST.matches(httpRequest.getMethod());
  }

}
