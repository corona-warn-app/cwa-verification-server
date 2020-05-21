package app.coronawarn.verification.services.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"log4j.configurationFile=log4j2-test.xml"})
@TestPropertySource("classpath:test.properties")
public class HashingServiceTest {
    static final Logger LOG = LogManager.getLogger();

    @Autowired
    HashingService hashingService;

    @Test
    public void testValidSha256Hash(){
        String hash = "523463041ef9ffa2950d8450feb34c88bc8692c40c9cf3c99dcdf75e270229e2";
        Boolean result = hashingService.isHashValid(hash);

        assertTrue(result);
    }

    @Test
    public void testInvalidSha256Hash(){
        String hash = "523463041ef9ffa2950d8z50feb34c88bc8692c40c9cf3c99dcdf75e270229e2";
        Boolean result = hashingService.isHashValid(hash);

        assertFalse(result);
    }


}
