package app.coronawarn.verification.factories.tan.verifier;

import app.coronawarn.verification.model.AppSessionSourceOfTrust;

public class TanVerifierFactoryImpl implements TanVerifierFactory {
  @Override
  public TanVerifier makeTanVerifier(AppSessionSourceOfTrust appSessionSourceOfTrust) {
    switch (appSessionSourceOfTrust) {
      case HASHED_GUID:
        return new HashedGuidTanVerifier();
      case TELETAN:
        return new TeletanTanVerifier();
      default:
        return new UnknownTanVerifier();
    }
  }
}
