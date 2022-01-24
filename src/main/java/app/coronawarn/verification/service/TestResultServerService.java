/*-
 * ---license-start
 * Corona-Warn-App / cwa-verification
 * ---
 * Copyright (C) 2020 - 2022 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.verification.service;

import app.coronawarn.verification.client.TestResultServerClient;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.TestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class represents the lab server service.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TestResultServerService {

  private final TestResultServerClient testResultServerClient;

  /**
   * This method gives an TestResult for a guid.
   *
   * @param guid hashed GUID
   * @return Testresult for GUID
   */
  public TestResult result(HashedGuid guid) {
    return testResultServerClient.result(guid);
  }
}
