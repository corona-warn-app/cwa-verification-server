package app.coronawarn.verification.factories.tan.verifier;

import app.coronawarn.verification.model.AppSessionSourceOfTrust;

public class TanVerifierFactoryImpl implements TanVerifierFactory {
  @Override
  public TanVerifier makeTanVerifier(AppSessionSourceOfTrust appSessionSourceOfTrust) {
    switch (appSessionSourceOfTrust) {
      case HASHED_GUID:
        return null;
      case TELETAN:
        return null;
      default:
        return null;
    }
  }
}
