package app.coronawarn.verification.factories.tan.verifier;

import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.exception.VerificationServerException;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.service.TestResultServerService;
import org.springframework.http.HttpStatus;
import org.springframework.util.StopWatch;

public class UnknownTanVerifier extends TanVerifier {
  @Override
  public void generateTan(VerificationAppSession appSession,
                          TestResultServerService testResultServerService,
                          StopWatch stopWatch,
                          TanSourceOfTrust tanSourceOfTrust) {
    stopWatch.stop();
    throw new VerificationServerException(HttpStatus.BAD_REQUEST,
      "Unknown source of trust inside the appsession for the registration token");
  }
}
