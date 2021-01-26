# Changelog

## v1.2.2 (05/08/2020)

#### Enhancements

- feat: changed Responsepadding to only appear if fake header exists [#197](https://github.com/corona-warn-app/cwa-verification-server/pull/197)

#### Bug Fixes

- fix: added json ignore if null [#198](https://github.com/corona-warn-app/cwa-verification-server/pull/198)
- fix: used corret fake delay [#200](https://github.com/corona-warn-app/cwa-verification-server/pull/200)
- fix: added delay for all endpoints [#201](https://github.com/corona-warn-app/cwa-verification-server/pull/201)

---

## v1.2.1 (27/07/2020)

#### Bug Fixes

- fix: temporary remove response padding [#194](https://github.com/corona-warn-app/cwa-verification-server/pull/194)
- fix: update openAPI docs to meet v1.2.0 [#192](https://github.com/corona-warn-app/cwa-verification-server/pull/192)

#### Others

- doc: documentation of feature 'fake header' [#191](https://github.com/corona-warn-app/cwa-verification-server/pull/191)

---

## v1.2.0 (22/07/2020)

#### Enhancements

- feat: feature plausible deniability (aka fake header) [#188](https://github.com/corona-warn-app/cwa-verification-server/pull/188)
- feat: added rate limiting for TeleTan creation [#172](https://github.com/corona-warn-app/cwa-verification-server/pull/172)
- feat: split internal and external endpoints [#184](https://github.com/corona-warn-app/cwa-verification-server/pull/184)

#### Bug Fixes

- fix: add a simple filter which prevents BSI issue #55 (request size limitation) [#91](https://github.com/corona-warn-app/cwa-verification-server/pull/91)

#### Documentation

- doc: rate limit, teleTAN crypto spec [#187](https://github.com/corona-warn-app/cwa-verification-server/pull/187)

---

## v1.1.0 (07/07/2020)

#### Enhancements

- feat: improve logs for cdc and increase spring version [#178](https://github.com/corona-warn-app/cwa-verification-server/pull/178)
- feat: automatic release notes generation [#175](https://github.com/corona-warn-app/cwa-verification-server/pull/175)
- feat: custom validator for Registration Token Request [#169](https://github.com/corona-warn-app/cwa-verification-server/pull/169)
- feat: use license-maven-plugin to allow generation of license file headers [#103](https://github.com/corona-warn-app/cwa-verification-server/pull/103)
- feat: speed up unit tests [#167](https://github.com/corona-warn-app/cwa-verification-server/pull/167)
- feat: improvement-logs-cdc [#177](https://github.com/corona-warn-app/cwa-verification-server/pull/177)
- feat: improve logs regarding cdc [#155](https://github.com/corona-warn-app/cwa-verification-server/pull/155)
- feat: removed unnecessary complexity [#165](https://github.com/corona-warn-app/cwa-verification-server/pull/165)

#### Bug Fixes

- fix: fix typos in architecture overview [#182](https://github.com/corona-warn-app/cwa-verification-server/pull/182)
- fix: added Glen to codeowners, deleted DockerfilePaaS [#174](https://github.com/corona-warn-app/cwa-verification-server/pull/174)
- fix: minor code quality improvements [#160](https://github.com/corona-warn-app/cwa-verification-server/pull/160)
- fix: readme.md [#181](https://github.com/corona-warn-app/cwa-verification-server/pull/181)
- fix: updated the release to 1.0.1 in POM-File [#171](https://github.com/corona-warn-app/cwa-verification-server/pull/171)
- fix: enable container scanning of distroless container [#173](https://github.com/corona-warn-app/cwa-verification-server/pull/173)
- fix: integration-JWT [#157](https://github.com/corona-warn-app/cwa-verification-server/pull/157)

#### Documentation

- doc: typo in API description [#170](https://github.com/corona-warn-app/cwa-verification-server/pull/170)
- doc: corrected teleTAN specification in architecture-overview.md [#159](https://github.com/corona-warn-app/cwa-verification-server/pull/159)
- doc: update architecture-overview.md [#180](https://github.com/corona-warn-app/cwa-verification-server/pull/180)
- doc: update contact mail in documents [#176](https://github.com/corona-warn-app/cwa-verification-server/pull/176)
- doc: reference 'Create Registration Token for E06.01' [#41](https://github.com/corona-warn-app/cwa-verification-server/pull/41)
- doc: update figures to match solution architecture [#116](https://github.com/corona-warn-app/cwa-verification-server/pull/116)

---

## v1.0.0 (12/06/2020)

#### Documentation

- Update README [#156](https://github.com/corona-warn-app/cwa-verification-server/pull/156)

---

## v0.6.1 (09/06/2020)
Release candidate
* mTLS support fix

---

## 0.6.0 (08/06/2020)
Release Candidate
---

## fix uppercase teleTan (01/06/2020)

---

## Beta API fix (31/05/2020)

---

## v0.5.1-beta (31/05/2020)
v0.5.1-beta fo cwa-verification-server
---

## v0.3.1-alpha (22/05/2020)
Initial public alpha release.