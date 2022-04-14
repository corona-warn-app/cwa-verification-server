package app.coronawarn.verification.factories.tan.verifier;

import app.coronawarn.verification.model.AppSessionSourceOfTrust;

public interface TanVerifierFactory {
  public TanVerifier makeTanVerifier(AppSessionSourceOfTrust appSessionSourceOfTrust);
}
