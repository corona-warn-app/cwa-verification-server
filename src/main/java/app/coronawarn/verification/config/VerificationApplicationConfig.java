package app.coronawarn.verification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This class is used to read in values from configuration file application.yml.
 * It is loaded via the @EnableConfigurationProperties annotation from SpringBootApplication main class. */
@ConfigurationProperties
public class VerificationApplicationConfig {

  public static class TeleCfg {
    public static class TeleValidCfg {
      @Getter @Setter private int hours;
    }

    @Getter @Setter private TeleValidCfg valid;
  }

  public static class ValidCfg {
    @Getter @Setter int days;
  }

  public static class TanCfg {
    @Getter @Setter private TeleCfg tele;
    @Getter @Setter private ValidCfg valid;
  }

  public static class AppSessionCfg {
    @Getter @Setter int tancountermax;
  }
  
  @Getter @Setter private TanCfg tan;
  @Getter @Setter private AppSessionCfg appsession;

}
