package app.coronawarn.verification.factories.tan.verifier;

import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.service.TestResultServerService;
import org.springframework.util.StopWatch;

public abstract class TanVerifier {
  public abstract void generateTan(VerificationAppSession appSession,
                                   TestResultServerService testResultServerService,
                                   StopWatch stopWatch,
                                   TanSourceOfTrust tanSourceOfTrust);
}
