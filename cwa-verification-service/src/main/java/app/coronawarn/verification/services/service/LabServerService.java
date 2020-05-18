/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, T-Systems International GmbH
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

package app.coronawarn.verification.services.service;

import app.coronawarn.verification.services.common.LabTestResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * This class represents the Labor Server service.
 *
 * @author T-Systems International GmbH
 */
@Component
public class LabServerService
{
    private static final Logger LOG = LogManager.getLogger();
    
    /**
     * The uri of the lab server result service
     */
    @Value("${uri.endpoint.labserver.result}")
    private String uri;    
    
    private final RestTemplate restTemplate = new RestTemplate();


    /**
     * Persists the specified entity of {@link VerficationAppSession} instances.
     *
     * @param guidHash
     * @return lab server result
     */
    public Integer callLabServerResult(String guidHash) {
        LOG.info("LabServerService start callLabServerResult.");
        return restTemplate.postForObject( uri, guidHash, Integer.class);
    }
}
