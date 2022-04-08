package app.coronawarn.verification.factories.tan.verifier;

import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.service.TestResultServerService;
import org.springframework.util.StopWatch;

public class TeletanTanVerifier extends TanVerifier {

  @Override
  public void generateTan(VerificationAppSession appSession,
                          TestResultServerService testResultServerService,
                          StopWatch stopWatch,
                          TanSourceOfTrust tanSourceOfTrust) {
    tanSourceOfTrust = TanSourceOfTrust.TELETAN;
  }
}
