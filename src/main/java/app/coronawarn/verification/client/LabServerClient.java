/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, T-Systems International GmbH
 *
 * Deutsche Telekom AG and all other contributors /
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

package app.coronawarn.verification.client;

import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.TestResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * This class represents the Labor Server service client.
 */
@FeignClient(name = "labServerService", url = "${uri.endpoint.labserver}")
public interface LabServerClient {
  @PostMapping(value = "/api/v1/app/result",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  TestResult result(HashedGuid guid);
}
