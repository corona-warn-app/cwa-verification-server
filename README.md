<h1 align="center">
    Corona-Warn-App Verification Server
</h1>

<p align="center">
    <a href="https://github.com/corona-warn-app/cwa-verification-server/commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/corona-warn-app/cwa-verification-server?style=flat"></a>
    <a href="https://github.com/corona-warn-app/cwa-verification-server/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-verification-server?style=flat"></a>
    <a href="https://github.com/corona-warn-app/cwa-verification-server/blob/master/LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#development">Development</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#support-and-feedback">Support</a> •
  <a href="#how-to-contribute">Contribute</a> •
  <a href="#contributors">Contributors</a> •
  <a href="#repositories">Repositories</a> •
  <a href="#licensing">Licensing</a>
</p>

The goal of this project is to develop the official Corona-Warn-App for Germany based on the exposure notification API from [Apple](https://www.apple.com/covid19/contacttracing/) and [Google](https://www.google.com/covid19/exposurenotifications/). The apps (for both iOS and Android) use Bluetooth technology to exchange anonymous encrypted data with other mobile phones (on which the app is also installed) in the vicinity of an app user's phone. The data is stored locally on each user's device, preventing authorities or other parties from accessing or controlling the data. This repository contains the **verification service** for the Corona-Warn-App.

## Status
![ci](https://github.com/corona-warn-app/cwa-verification-server/workflows/ci/badge.svg)
[![quality gate](https://sonarcloud.io/api/project_badges/measure?project=corona-warn-app_cwa-verification-server&metric=alert_status)](https://sonarcloud.io/dashboard?id=corona-warn-app_cwa-verification-server)
[![coverage](https://sonarcloud.io/api/project_badges/measure?project=corona-warn-app_cwa-verification-server&metric=coverage)](https://sonarcloud.io/dashboard?id=corona-warn-app_cwa-verification-server)
[![bugs](https://sonarcloud.io/api/project_badges/measure?project=corona-warn-app_cwa-verification-server&metric=bugs)](https://sonarcloud.io/dashboard?id=corona-warn-app_cwa-verification-server)

## About this component

In the world of the Corona Warn App the Verification Server helps validating whether upload requests from the mobile App are valid or not. The parts of the verification component cooperate in the following manner:

- The Verification Server of the Corona Warn App (repository: cwa-verification-server) helps validating whether upload requests from the mobile App are valid or not.
- The Verification Portal of the Corona Warn App (repository: cwa-verification-portal) allows hotline employees to generate teleTANs which are used by users of the mobile App to upload their diagnostic keys.
- The Verification Identity and Access of the Corona Warn App (repository: cwa-verification-iam) ensures that only authorized health personnel get access to the Verification Portal.
- The Test Result Server of the Corona Warn App (repository: cwa-testresult-server) receives the results from laboratories and delivers these results to the app via the verification-server.

## Architecture overview
You can find an architectural overview of the component in the [solution architecture document](https://github.com/corona-warn-app/cwa-documentation/blob/master/solution_architecture.md).  
This component of the Corona-Warn-App whereas named **verification process** provides indeed two functionalities:  
1. prove that a pretended positive case is indeed positive  
2. provide the result of a COVID-19 test  

To achieve this, the verification service gets the result of COVID-19 tests from LIS (**L**abor **I**nformation **S**ystem) which delivers test results to it. The complete process is described in [cwa-documentation/Solution Architecture](https://github.com/corona-warn-app/cwa-documentation/blob/master/solution_architecture.md) to which you may refer for detailed information about the workflow.

The software stack of the verification server is based on [Spring Boot](https://spring.io/projects/spring-boot), currently with an in-memory H2 database. As the persistence relies on [Liquibase](https://www.liquibase.org).

## Development
This component can be locally build in order to test the functionality of the interfaces and verify the concepts it is built upon.  

There are two ways to build:
 - [Maven](https:///maven.apache.org) build - to run this component as spring application on your local machine
 - [Docker](https://www.docker.com) build - to run it as docker container build from the provided docker build [file](https://github.com/corona-warn-app/cwa-verification-server/blob/master/Dockerfile)

### Prerequisites
 - [Open JDK 11](https://openjdk.java.net)  
 - [Maven](https://maven.apache.org)
 - *(optional)*: [Docker](https://www.docker.com)  

### Build
Whether you cloned or downloaded the 'zipped' sources you will either find the sources in the chosen checkout-directory or get a zip file with the source code, which you can expand to a folder of your choice.

In either case open a terminal pointing to the directory you put the sources in. The local build process is described afterwards depending on the way you choose.

#### Maven based build
This is the recommended way for taking part in the development.  
Please check, whether following prerequisites are installed on your machine:
- [Open JDK 11](https://openjdk.java.net) or a similar JDK 11 compatible VM  
- [Maven](https://maven.apache.org)

You can then open a terminal pointing to the root directory of the verification server and do the following:

    mvn package
    java -jar target/cwa-verification-server-0.0.1-SNAPSHOT.jar  

The verification server will start up and run locally on your machine available on port 8080.

#### Docker based build  
We recommend that you first check to ensure that [Docker](https://www.docker.com) is installed on your machine.

On the command line do the following:
```bash
docker build -f|--file <path to dockerfile>  -t <imagename>  <path-to-verificationserver-root>
docker run -p 127.0.0.1:8080:8080/tcp -it <imagename>
```
or simply  
```bash
docker build --pull --rm -f "Dockerfile" -t cwa-verificationserver "."
docker run -p 127.0.0.1:8080:8080/tcp -it cwa-verificationserver
```
if you are in the root of the checked out repository.  
The docker image will then run on your local machine on port 8080 assuming you configured docker for shared network mode.

#### API documentation  
Along with the application there comes a [swagger2](https://swagger.io) API documentation, which you can access in your web browser when the verification server applications runs:

    <base-url>/api/swagger

Which results in the following URL on your local machine:
http://localhost:8080/api/swagger

#### Remarks
This repository contains files which support our CI/CD pipeline and will be removed without further notice  
 - DockerfileCi - used for the GitHub build chain
 - Jenkinsfile - used for Telekom internal SBS (**S**oftware**B**uild**S**ervice)

## Documentation  
The full documentation for the Corona-Warn-App can be found in the [cwa-documentation](https://github.com/corona-warn-app/cwa-documentation) repository. The documentation repository contains technical documents, architecture information, and white papers related to this implementation.

## Support and feedback
The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **General Discussion**   | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="General Discussion"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/question.svg?style=flat-square"></a> </a>   |
| **Concept Feedback**    | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="Open Concept Feedback"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/architecture.svg?style=flat-square"></a>  |
| **Verification server issues**    | <a href="https://github.com/corona-warn-app/cwa-verification-server/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-verification-server?style=flat"></a>  |
| **Other requests**    | <a href="mailto:opensource@telekom.de" title="Email CWA Team"><img src="https://img.shields.io/badge/email-CWA%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## How to contribute  
Contribution and feedback is encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors  
The German government has asked SAP AG and Deutsche Telekom AG to develop the Corona-Warn-App for Germany as open source software. Deutsche Telekom is providing the network and mobile technology and will operate and run the backend for the app in a safe, scalable and stable manner. SAP is responsible for the app development, its framework and the underlying platform. Therefore, development teams of SAP and Deutsche Telekom are contributing to this project. At the same time our commitment to open source means that we are enabling -in fact encouraging- all interested parties to contribute and become part of its developer community.

## Repositories

A list of all public repositories from the Corona-Warn-App can be found [here](https://github.com/corona-warn-app/cwa-documentation/blob/master/README.md#repositories).

## Licensing
Copyright (c) 2020 Deutsche Telekom AG.

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.
