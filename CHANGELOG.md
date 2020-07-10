# Changelog

## v1.0.0 (12/06/2020)

#### Documentation

- Update README [#156](https://github.com/corona-warn-app/cwa-verification-server/pull/156)

---

## v0.6.1 (09/06/2020)

#### Bug Fixes

- fix: trusted container [#154](https://github.com/corona-warn-app/cwa-verification-server/pull/154)

---

## v0.6.0 (08/06/2020)

#### Enhancements

- feat: monitoring [#124](https://github.com/corona-warn-app/cwa-verification-server/pull/124)
- feat: client for testresultserver mtls [#143](https://github.com/corona-warn-app/cwa-verification-server/pull/143)
- feat: add feature flag [#140](https://github.com/corona-warn-app/cwa-verification-server/pull/140)
- feature: add mTLS configuration for verification-server to testresult… [#136](https://github.com/corona-warn-app/cwa-verification-server/pull/136)
- feat: change delete timeframe to 21 days [#120](https://github.com/corona-warn-app/cwa-verification-server/pull/120)
- feat: ci trusted [#132](https://github.com/corona-warn-app/cwa-verification-server/pull/132)
- feat: monitoring security [#125](https://github.com/corona-warn-app/cwa-verification-server/pull/125)
- feat: changed Tele Tan length to be configurable [#149](https://github.com/corona-warn-app/cwa-verification-server/pull/149)
- feat: set JWT enabled configurable [#147](https://github.com/corona-warn-app/cwa-verification-server/pull/147)
- feat: ratelimiting-teletan-format [#142](https://github.com/corona-warn-app/cwa-verification-server/pull/142)
- Feat: JWT implementation for the teleTAN request [#134](https://github.com/corona-warn-app/cwa-verification-server/pull/134)
- Update architecture-overview.md and default configuration [#138](https://github.com/corona-warn-app/cwa-verification-server/pull/138)
- feat: integration of result server status redeemed and docu improvements [#122](https://github.com/corona-warn-app/cwa-verification-server/pull/122)
- feat: spring security integration and logging [#123](https://github.com/corona-warn-app/cwa-verification-server/pull/123)

#### Bug Fixes

- fix: fix #126 codespell and remove redundant properties from application-cloud.yml [#129](https://github.com/corona-warn-app/cwa-verification-server/pull/129)
- fix: codeowners [#148](https://github.com/corona-warn-app/cwa-verification-server/pull/148)
- fix: spring head [#137](https://github.com/corona-warn-app/cwa-verification-server/pull/137)
- fix: mtls feature flag [#141](https://github.com/corona-warn-app/cwa-verification-server/pull/141)
- fix: Teletan will be verified if it is expired [#127](https://github.com/corona-warn-app/cwa-verification-server/pull/127)
- fix: find the expected entity [#145](https://github.com/corona-warn-app/cwa-verification-server/pull/145)
- Fix: Issue #47- remove "not Null" for GUIDHash and teleTANHash [#135](https://github.com/corona-warn-app/cwa-verification-server/pull/135)

#### Documentation

- doc: User Story References [#42](https://github.com/corona-warn-app/cwa-verification-server/pull/42)

---

## v0.5.3 (01/06/2020)

#### Enhancements

- feat: cleanup code, apply code style, remove code smells [#115](https://github.com/corona-warn-app/cwa-verification-server/pull/115)

#### Bug Fixes

- Fix test and docu improvements [#111](https://github.com/corona-warn-app/cwa-verification-server/pull/111)
- fix: get test state [#118](https://github.com/corona-warn-app/cwa-verification-server/pull/118)
- fix:teleTan [#119](https://github.com/corona-warn-app/cwa-verification-server/pull/119)

---

## v0.5.2 (31/05/2020)

#### Enhancements

- feat: mvn version sha [#112](https://github.com/corona-warn-app/cwa-verification-server/pull/112)

#### Bug Fixes

- fix: set to next snapshot version [#109](https://github.com/corona-warn-app/cwa-verification-server/pull/109)
- fix: api changes for testing [#113](https://github.com/corona-warn-app/cwa-verification-server/pull/113)
- fix:  revert api change [#114](https://github.com/corona-warn-app/cwa-verification-server/pull/114)

---

## v0.5.1-beta (31/05/2020)

#### Enhancements

- Use enums as values instead of strings [#79](https://github.com/corona-warn-app/cwa-verification-server/pull/79)
- feat: sonar [#58](https://github.com/corona-warn-app/cwa-verification-server/pull/58)
- Update on OTC subscription/namespace structure [#71](https://github.com/corona-warn-app/cwa-verification-server/pull/71)
- feat: spring actuator to monitor the health of the application [#83](https://github.com/corona-warn-app/cwa-verification-server/pull/83)
- fix: Validation on Input Parameter (Issue #33) [#102](https://github.com/corona-warn-app/cwa-verification-server/pull/102)
- feat: implementation of the cleanup concept [#101](https://github.com/corona-warn-app/cwa-verification-server/pull/101)
- fix: add Teletan to TestResult [#96](https://github.com/corona-warn-app/cwa-verification-server/pull/96)
- feat: spring improvements [#82](https://github.com/corona-warn-app/cwa-verification-server/pull/82)
- fix: gitattributes [#81](https://github.com/corona-warn-app/cwa-verification-server/pull/81)
- fix: ci remove set env [#72](https://github.com/corona-warn-app/cwa-verification-server/pull/72)
- Fix/Add JUnit-Tests to increase the codecoverage [#76](https://github.com/corona-warn-app/cwa-verification-server/pull/76)
- Fix TAN_TAN_PATTERN to include regex meta characters [#65](https://github.com/corona-warn-app/cwa-verification-server/pull/65)
- feat: ci sonar disabled for pr [#70](https://github.com/corona-warn-app/cwa-verification-server/pull/70)
- feat: ci pr improvements [#66](https://github.com/corona-warn-app/cwa-verification-server/pull/66)
- fix additional junit tests, clean code, bugfixes [#51](https://github.com/corona-warn-app/cwa-verification-server/pull/51)
- feat: ci refactor [#57](https://github.com/corona-warn-app/cwa-verification-server/pull/57)

#### Bug Fixes

- fix: readme [#93](https://github.com/corona-warn-app/cwa-verification-server/pull/93)
- fix: spring config issues [#89](https://github.com/corona-warn-app/cwa-verification-server/pull/89)
- fix: rename lab server to test result server [#100](https://github.com/corona-warn-app/cwa-verification-server/pull/100)
- Fiy the test result name in the readme [#99](https://github.com/corona-warn-app/cwa-verification-server/pull/99)
- fix: actuator properties moved to application-cloud.yml [#84](https://github.com/corona-warn-app/cwa-verification-server/pull/84)
- fix: rename test class for use in build process [#68](https://github.com/corona-warn-app/cwa-verification-server/pull/68)
- fix: updated guava to newest version [#78](https://github.com/corona-warn-app/cwa-verification-server/pull/78)
- Teletan matched string with [characters]{7} and thus also matched 8 c… [#44](https://github.com/corona-warn-app/cwa-verification-server/pull/44)
- doc: fix a typo [#77](https://github.com/corona-warn-app/cwa-verification-server/pull/77)
- fix: remove author tags. Closes #62 [#90](https://github.com/corona-warn-app/cwa-verification-server/pull/90)
- fix: broken maven website links [#106](https://github.com/corona-warn-app/cwa-verification-server/pull/106)
- fix: the usage of JPA findOne [#97](https://github.com/corona-warn-app/cwa-verification-server/pull/97)
- fix: spring repackage [#75](https://github.com/corona-warn-app/cwa-verification-server/pull/75)

#### Documentation

- fix: readme repository links [#95](https://github.com/corona-warn-app/cwa-verification-server/pull/95)
- Fixed a comment that incorrectly lists available enum values. [#98](https://github.com/corona-warn-app/cwa-verification-server/pull/98)
- improve README.md [#63](https://github.com/corona-warn-app/cwa-verification-server/pull/63)
- Update architecture-overview.md [#56](https://github.com/corona-warn-app/cwa-verification-server/pull/56)
- fix typo in CONTRIBUTING.md [#48](https://github.com/corona-warn-app/cwa-verification-server/pull/48)
- fix: documentation [#73](https://github.com/corona-warn-app/cwa-verification-server/pull/73)

---

## v0.3.1-alpha (22/05/2020)

#### Enhancements

- feature: github issue templates [#11](https://github.com/corona-warn-app/cwa-verification-server/pull/11)

#### Documentation

- doc: updates to README [#10](https://github.com/corona-warn-app/cwa-verification-server/pull/10)
