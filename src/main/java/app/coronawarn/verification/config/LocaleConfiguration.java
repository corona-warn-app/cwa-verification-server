package app.coronawarn.verification.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;


@Configuration
public abstract class LocaleConfiguration extends AcceptHeaderLocaleResolver implements WebMvcConfigurer {

  List<Locale> locales = Arrays.asList(
    new Locale("ru"),
    new Locale("de"),
    new Locale("en"));

  /**
   * * @return default Locale set by the user.
   */
  @Bean(name = "localeResolver")
  public LocaleResolver localeResolver(HttpServletRequest request) {
    String headerLang = request.getHeader("Accept-Language");
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(headerLang == null || headerLang.isEmpty()
      ? new Locale("en")
      : Locale.lookup(Locale.LanguageRange.parse(headerLang), locales));
    return slr;
  }

  /**
   * * @return message source.
   */
  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource rs = new ResourceBundleMessageSource();
    rs.setBasename("messages");
    rs.setDefaultEncoding("UTF-8");
    rs.setUseCodeAsDefaultMessage(true);
    return rs;
  }
}
