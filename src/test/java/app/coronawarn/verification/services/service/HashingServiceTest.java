package app.coronawarn.verification.services.service;

import app.coronawarn.verification.services.VerificationApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VerificationApplication.class)
public class HashingServiceTest {
    static final Logger LOG = LogManager.getLogger();

    @Autowired
    HashingService hashingService;

    @Test
    public void testValidSha256Hash(){
        String hash = "523463041ef9ffa2950d8450feb34c88bc8692c40c9cf3c99dcdf75e270229e2";
        boolean result = hashingService.isHashValid(hash);

        assertTrue(result);
    }

    @Test
    public void testInvalidSha256Hash(){
        String hash = "523463041ef9ffa2950d8z50feb34c88bc8692c40c9cf3c99dcdf75e270229e2";
        boolean result = hashingService.isHashValid(hash);

        assertFalse(result);
    }


}
