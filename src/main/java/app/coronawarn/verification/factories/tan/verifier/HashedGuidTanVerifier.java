package app.coronawarn.verification.factories.tan.verifier;

import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.exception.VerificationServerException;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.LabTestResult;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.model.TestResult;
import app.coronawarn.verification.service.TestResultServerService;
import org.springframework.http.HttpStatus;
import org.springframework.util.StopWatch;

public class HashedGuidTanVerifier extends TanVerifier {
  @Override
  public void generateTan(VerificationAppSession appSession,
                          TestResultServerService testResultServerService,
                          StopWatch stopWatch,
                          TanSourceOfTrust tanSourceOfTrust) {
    TestResult covidTestResult = testResultServerService.result(new HashedGuid(appSession.getHashedGuid()));
    if (covidTestResult.getTestResult() != LabTestResult.POSITIVE.getTestResult()
      && covidTestResult.getTestResult() != LabTestResult.QUICK_POSITIVE.getTestResult()
    ) {
      stopWatch.stop();
      throw new VerificationServerException(HttpStatus.BAD_REQUEST,
        "Tan cannot be created, caused by the non positive result of the labserver");
    }
  }
}
