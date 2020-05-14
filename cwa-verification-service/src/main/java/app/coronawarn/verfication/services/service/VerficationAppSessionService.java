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

package app.coronawarn.verfication.services.service;

import app.coronawarn.verfication.services.domain.CoronaVerficationAppSession;
import app.coronawarn.verfication.services.repository.AppSessionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

/**
 * This class represents the CoronaVerficationAppSession service.
 *
 * @author A336717, T-Systems International GmbH
 */
public class VerficationAppSessionService
{
    private static final Logger LOG = LogManager.getLogger();

    @Autowired
    private AppSessionRepository appSessionRepository;

    /**
     * Persists the specified entity of {@link CoronaVerficationAppSession} instances.
     *
     * @param appSession the verification app session entity
     */
    public void saveAppSession(CoronaVerficationAppSession appSession) {
        LOG.info("VerficationAppSessionService start saveAppSession.");
        appSessionRepository.save(appSession);
    }    
    
    /**
     * Check for existing Reg Token in the {@link AppSessionRepository}.
     *
     * @param registrationTokenHash the hashed registrationToken
     * @return flag for existing registrationToken
     */
    public boolean checkRegistrationTokenExists(String registrationTokenHash) {
        LOG.info("VerficationAppSessionService start checkRegistrationTokenExists.");
        CoronaVerficationAppSession appSession = new CoronaVerficationAppSession();
        appSession.setRegistrationTokenHash(registrationTokenHash);
        return appSessionRepository.exists(Example.of(appSession, ExampleMatcher.matchingAll()));
    } 
    
}
