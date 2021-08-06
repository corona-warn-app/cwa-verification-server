package app.coronawarn.verification;

import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.domain.VerificationTan;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.AuthorizationRole;
import app.coronawarn.verification.model.TanSourceOfTrust;
import app.coronawarn.verification.model.TanType;
import app.coronawarn.verification.model.TeleTanType;
import app.coronawarn.verification.model.TestResult;
import app.coronawarn.verification.repository.VerificationAppSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {

  static final String TEST_GUI_HASH = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
  static final String TEST_GUI_HASH2 = "a1f7347308703928470938247903247903274903274903297041bea091d14d4d";
  static final String TEST_GUI_HASH_DOB = "x0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
  static final String TEST_GUI_HASH_DOB2 = "x1f7347308703928470938247903247903274903274903297041bea091d14d4d";
  static final String RESULT_PADDING = "";
  static final String LAB_ID = "l".repeat(64);
  static final String TEST_INVALID_GUI_HASH = "f0e4c2f76c58916ec2b";
  static final String TEST_TELE_TAN = "R3ZNUeV";
  static final String TEST_TELE_TAN_HASH = "eeaa54dc40aa84f587e3bc0cbbf18f7c05891558a5fe1348d52f3277794d8730";
  static final String TEST_INVALID_REG_TOK = "1234567890";
  static final String TEST_REG_TOK = "1ea6ce8a-9740-41ea-bb37-0242ac130002";
  static final String TEST_REG_TOK_HASH = "0199effab87800689c15c08e234db54f088cc365132ffc230e882b82cd3ecf95";
  static final TestResult TEST_LAB_POSITIVE_RESULT = new TestResult(2,0, LAB_ID, null);
  static final TestResult QUICK_TEST_POSITIVE_RESULT = new TestResult(7,0, LAB_ID, null);
  static final TestResult TEST_LAB_NEGATIVE_RESULT = new TestResult(1,0, LAB_ID, null);
  static final String TEST_TAN = "1819d933-45f6-4e3c-80c7-eeffd2d44ee6";
  static final String TEST_INVALID_TAN = "1ea6ce8a-9740-11ea-is-invalid";
  static final TanSourceOfTrust TEST_SOT = TanSourceOfTrust.CONNECTED_LAB;
  static final String TEST_HASHED_TAN = "cfb5368fc0fca485847acb28e6a96c958bb6ab7350ac766be88ad13841750231";
  static final TanType TEST_TAN_TYPE = TanType.TAN;
  static final LocalDateTime TAN_VALID_UNTIL_IN_DAYS = LocalDateTime.now().plusDays(7);
  static final String PREFIX_API_VERSION = "/version/v1";
  static final String REGISTRATION_TOKEN_URI = "/registrationToken";
  static final String TAN_VERIFICATION_URI = "/tan/verify";

  private static ObjectMapper objectMapper;

  @Autowired
  public TestUtils(ObjectMapper objectMapper) {
    TestUtils.objectMapper = objectMapper;
  }

  static void prepareAppSessionTestData(VerificationAppSessionRepository appSessionRepository) {
    appSessionRepository.deleteAll();
    appSessionRepository.save(getAppSessionTestData());
  }

  static void prepareAppSessionTestDataDob(VerificationAppSessionRepository appSessionRepository) {
    appSessionRepository.deleteAll();
    appSessionRepository.save(getAppSessionTestData(AppSessionSourceOfTrust.HASHED_GUID, true));
  }

  static void prepareAppSessionTestDataSotTeleTan(VerificationAppSessionRepository appSessionRepository) {
    appSessionRepository.deleteAll();
    appSessionRepository.save(getAppSessionTestData(AppSessionSourceOfTrust.TELETAN, false));
  }

  static VerificationAppSession getAppSessionTestData(AppSessionSourceOfTrust sot, boolean dob) {
    VerificationAppSession cv = new VerificationAppSession();
    cv.setTeleTanType(TeleTanType.EVENT);
    cv.setHashedGuid(TEST_GUI_HASH);
    cv.setHashedGuidDob(dob ? TEST_GUI_HASH_DOB : null);
    cv.setCreatedAt(LocalDateTime.now());
    cv.setUpdatedAt(LocalDateTime.now());
    cv.setTanCounter(0);
    cv.setSourceOfTrust(sot);
    cv.setRegistrationTokenHash(TEST_REG_TOK_HASH);
    return cv;
  }

  static VerificationAppSession getAppSessionTestData() {
    return getAppSessionTestData(AppSessionSourceOfTrust.HASHED_GUID, false);
  }

  static VerificationTan getTeleTanTestData() {
    VerificationTan cvtan = new VerificationTan();
    cvtan.setTeleTanType(TeleTanType.EVENT);
    cvtan.setCreatedAt(LocalDateTime.now());
    cvtan.setUpdatedAt(LocalDateTime.now());
    cvtan.setRedeemed(false);
    cvtan.setSourceOfTrust(TanSourceOfTrust.TELETAN);
    cvtan.setTanHash(TEST_HASHED_TAN);
    cvtan.setType(TanType.TELETAN);
    cvtan.setValidFrom(LocalDateTime.now());
    cvtan.setValidUntil(LocalDateTime.now().plusHours(1));
    return cvtan;
  }

  static VerificationTan getVerificationTANTestData() {
    VerificationTan cvtan = new VerificationTan();
    cvtan.setTeleTanType(TeleTanType.EVENT);
    cvtan.setCreatedAt(LocalDateTime.now());
    cvtan.setUpdatedAt(LocalDateTime.now());
    cvtan.setRedeemed(false);
    cvtan.setSourceOfTrust(TEST_SOT);
    cvtan.setTanHash(TEST_HASHED_TAN);
    cvtan.setType(TEST_TAN_TYPE);
    cvtan.setValidFrom(LocalDateTime.now().minusDays(5));
    cvtan.setValidUntil(TAN_VALID_UNTIL_IN_DAYS);
    return cvtan;
  }

  static String getAsJsonFormat(Object o) throws JsonProcessingException {
    return objectMapper.writeValueAsString(o);
  }

  static String getJwtTestData(final long expirationSecondsToAdd, PrivateKey privateKey, AuthorizationRole... roles) {
    final Map<String, List<String>> realmAccessMap = new HashMap<>();
    final List<String> roleNames = new ArrayList<>();
    for (AuthorizationRole role : roles) {
      roleNames.add(role.getRoleName());
    }

    realmAccessMap.put("roles", roleNames);

    return Jwts.builder()
      .setExpiration(Date.from(Instant.now().plusSeconds(expirationSecondsToAdd)))
      .setIssuedAt(Date.from(Instant.now()))
      .setId("baeaa733-521e-4d2e-8abe-95bb440a9f5f")
      .setIssuer("http://localhost:8080/auth/realms/cwa")
      .setAudience("account")
      .setSubject("72b3b494-a0f4-49f5-b235-1e9f93c86e58")
      .claim("auth_time", "1590742669")
      .claim("iss", "http://localhost:8080/auth/realms/cwa")
      .claim("aud", "account")
      .claim("typ", "Bearer")
      .claim("azp", "verification-portal")
      .claim("session_state", "41cc4d83-e394-4d08-b887-28d8c5372d4a")
      .claim("acr", "0")
      .claim("realm_access", realmAccessMap)
      .claim("resource_access", new HashMap<>())
      .claim("scope", "openid profile email")
      .claim("email_verified", false)
      .claim("preferred_username", "test")
      .signWith(privateKey, SignatureAlgorithm.RS256)
      .compact();
  }

}
