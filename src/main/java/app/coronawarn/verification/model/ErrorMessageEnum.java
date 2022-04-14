package app.coronawarn.verification.model;

import java.util.EnumSet;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

public enum ErrorMessageEnum {

  MAXIMUM_TAN_GENERATION("maximum.tan.generation"),
  VERIFICATIONAPPSESSION_NOT_FOUND("verificationappsession.not.found"),
  SSL_CONTEXT_COULD_NOT_LOAD("ssl.context.could.not.load"),
  TEST_RESULT_INCONSISTENT("test.result.inconsistent"),
  UNKNOWN_SOURCE_FOR_REGISTRATION_TOKEN("unknown.source.for.registration.token"),
  REGISTRATION_TOKEN_NOT_EXIST("registration.token.not.exist"),
  TAN_NOT_CREATED_POSITIVE_RESULT("tan.not.created.positive.result"),
  RETURN_TEST_RESULT_FAILED("return.test.result.failed"),
  TELETAN_VERIFICATION_FAILED("teletan.verification.failed"),
  UNKNOWN_REGISTRATION_KEY_TYPE("unknown.registration.key.type"),
  TAN_NOT_FOUND_OR_INVALID("tan.not.found.or.invalid"),
  RATE_LIMIT_EXCEEDED("rate.limit.exceeded"),
  INVALID_JWT("invalid.jwt"),
  INTERNAL_TESTSTATE_NOT_ALLOWED("internal.teststate.not.allowed"),
  REGISTRATION_TOKEN_NOT_FOUND("registration.token.not.found"),
  RETURN_SUCCESS_TAN("return.success.tan");

  private MessageSource messageSource;
  private String key = "";

  private void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Component
  public static class MessageSourceInjector {
    @Autowired
    private MessageSource messageSource;

    /**
     * * Initialize the message source for each enum.
     */
    @PostConstruct
    public void postConstruct() {
      for (ErrorMessageEnum enums : EnumSet.allOf(ErrorMessageEnum.class)) {
        enums.setMessageSource(messageSource);
      }
    }
  }

  ErrorMessageEnum(String key) {
    this.key = key;
  }

  ErrorMessageEnum() {
  }

  public String getMessage() {
    return messageSource.getMessage(this.key, new String[]{}, LocaleContextHolder.getLocale());
  }

}
