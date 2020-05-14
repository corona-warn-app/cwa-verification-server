/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, A336717, T-Systems International GmbH
 *
 * Deutsche Telekom AG, SAP AG and all other contributors /
 * copyright owners license this file to you under the Apache 
 * License, Version 2.0 (the "License"); you may not use this 
 * file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package app.coronawarn.verfication.services;

import app.coronawarn.verfication.services.domain.CoronaVerficationAppSession;
import app.coronawarn.verfication.services.domain.CoronaVerificationState;
import app.coronawarn.verfication.services.repository.AppSessionRepository;
import app.coronawarn.verfication.services.service.VerficationAppSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * This is the test class for the verification application, the spring boot JTA
 * application and rest controller. The persistence properties
 * TestPropertySource(H2 DB) is used. The Spring MVC Test project is used so we
 * can do full controller testing via unit tests. Since Spring-Data-Rest uses
 * Spring MVC internally we can use MockMvc.
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(properties = {"log4j.configurationFile=log4j2-test.xml"})
@TestPropertySource("classpath:test.properties")
public class CoronaVerficationAppTests {

    static final Logger LOG = LogManager.getLogger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private VerficationAppSessionService appSessionService;

    @Autowired
    private AppSessionRepository repository;

    public static final String TEST_GUI_HASH = "12542154785411";
    public static final String TEST_REG_TOK_HASH = "1234567890";

    /**
     * Test call generateTAN via Conroller with MockMvc from Spring Test.
     *
     * @throws Exception if the test cannot be performed.
     */
    @Test
    public void callGenerateTANViaController() throws Exception {
        LOG.info("CoronaVerficationAppTests callGenerateTANViaController() ");

        repository.deleteAll();

        CoronaVerficationAppSession cv = new CoronaVerficationAppSession();
        cv.setVerificationState(CoronaVerificationState.NEGATIVE);
        cv.setGuidHash(TEST_GUI_HASH);
        cv.setTanGenerated(true);
        cv.setCreatedOn(LocalDateTime.now());
        cv.setRegistrationTokenHash(TEST_REG_TOK_HASH);

        repository.save(cv);

        mockMvc.perform(post("/tan").contentType(MediaType.APPLICATION_JSON).content(TEST_GUI_HASH))
                .andExpect(status().isCreated());

        // List<CoronaVerficationAppSession> verfications = repository.findAll();

        long count = repository.count();
        LOG.info("Got {} verfication entries from db repository.", count);

        assertTrue("Verification Failed: Amount of verfication entries is not 1 (Result=" + count + "). ", count == 1);

        CoronaVerficationAppSession coronaVerfication = repository.getOne(count);
        assertNotNull(coronaVerfication);
        assertEquals(CoronaVerificationState.NEGATIVE, coronaVerfication.getVerificationState());
        assertEquals(TEST_GUI_HASH, coronaVerfication.getGuidHash());
        assertTrue(coronaVerfication.isTanGenerated());
        assertEquals(TEST_REG_TOK_HASH, coronaVerfication.getRegistrationTokenHash());
    }

    private String getAsJsonFormat(Object o) throws JsonProcessingException {
        String jsonRepresentation = mapper.writeValueAsString(o);
        return jsonRepresentation;
    }
}
