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

The goal of this project is to develop the official Corona-Warn-App for Germany based on the exposure notification API from [Apple](https://www.apple.com/covid19/contacttracing/) and [Google](https://www.google.com/covid19/exposurenotifications/). The apps (for both iOS and Android) use Bluetooth technology to exchange anonymous encrypted data with other mobile phones (on which the app is also installed) in the vicinity of an app user's phone. The data is stored locally on each user's device, preventing authorities or other parties from accessing or controlling the data. This repository contains the **verification service** for the Corona-Warn-App. This implementation is still a **work in progress**, and the code it contains is currently alpha-quality code.

## Architecture Overview
You can find an architectural overview of the component in the [solution architecture document](https://github.com/corona-warn-app/cwa-documentation/blob/master/solution_architecture.md)  
This component of the Corona-warn-app whereas named **verification process** provides indeed two functionalities:  
1. prove that a prentended positive case is indeed positive  
2. provide the result of a Covid-19 Test  
    
To achieve this, the verification service gets the result of covid-19 tests from LIS (**L**abor **I**nformation **S**ystem) which deliver testresults to it. The complete process is described in [cwa-documentation/Solution Architecture](https://github.com/corona-warn-app/cwa-documentation/blob/master/solution_architecture.md) to which you may refer for detailed information about the workflow.

The software stack of the verification server is based on spring boot, currently with an in-memory H2 database. As the persistence relies on the liquibase


## Development

This component can be locally build in order to test the functionality of the interfaces and verify the concepts it is build upon.  
There are two ways to build:
 - [Maven](https:///maven.apache.org) build - to run this component as spring application on your local machine
 - [Docker](https://www.docker.com) build - to run it as docker container build from the provided docker build [file](https://github.com/corona-warn-app/cwa-verification-server/blob/master/Dockerfile)
 ### Prerequisites
 [Open JDK 11](https://openjdk.java.net)  
 [Maven](https://apache.maven.org)  
 *(optional)*: [Docker](https://www.docker.com)  
 ### Build
 Whether you cloned or downloaded the 'ziped' souces you will either find the sources in the chosen checkout-directory or get a zip file with the source code, which you can expand to a folder of your choice.

 In either case open a terminal pointing to the directory you put the sources in. The local build process is described afterwards depending on the way you chose.
#### Maven based build
For acitvely take part on the development this is the way you should chose.   
Please check, whether following prerequisites are fullfilled
- [Open JDK 11](https://openjdk.java.net) or a similar JDK 11 compatible VM  
- [Maven](https://apache.maven.org)  
  
is installed on your machine.  
You can then open a terminal pointing to the root directory of the verification server and do the following:

    mvn package
    java -jar target/cwa-verification-server-0.0.1-SNAPSHOT.jar  

The verification server will start up and run locally on your machine available on port 8080.

#### Docker based build  
We recommend, that you first check the prerequisites to ensure that  
- [Docker](https://www.docker.com)  

is istalled on you machine  

On the commandline do the following:

    docker build -f|--file <path to dockerfile>  -t <imagename>  <path-to-verificationserver-root>
    docker run -p 127.0.0.1:8080:8080/tcp -it <imagename>
    
or simply  

    docker build --pull --rm -f "Dockerfile" -t cwa-verificationserver "."
    docker run -p 127.0.0.1:8080:8080/tcp -it cwa-verificationserver 

if you are in the root of the checked out repository.  
The dockerimmage will then run on your local machine on port 8080 assuming you configured docker for shared network mode.
#### API Documentation  

Along with the application there comes a swagger2 api documentation which you can access in your webbrowser, when the verification server applications runs:

    <base-url>/swagger-ui.html#/verification-controller

mostly like:  

http://localhost:8080/swagger-ui.html#/verification-controller
     


#### Remarks
This repository contains files which support our CI/CD pipeline and will be remove without further notice  
 - DockerfileCi - used for the GitHub buildchaing
 - Jenkinsfile - used for Telekom internal SBS (**S**oftware**B**uild**S**ervice)



## Documentation

The full documentation for the Corona-Warn-App can be found in the [cwa-documentation](https://github.com/corona-warn-app/cwa-documentation) repository. The documentation repository contains technical documents, architecture information, and white papers related to this implementation.

## Support and Feedback
The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **General Discussion**   | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="General Discussion"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/question.svg?style=flat-square"></a> </a>   |
| **Concept Feedback**    | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="Open Concept Feedback"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/architecture.svg?style=flat-square"></a>  |
| **Verification Server Issue**    | <a href="https://github.com/corona-warn-app/cwa-verification-server/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-verification-server?style=flat"></a>  |
| **Other Requests**    | <a href="mailto:corona-warn-app.opensource@sap.com" title="Email CWD Team"><img src="https://img.shields.io/badge/email-CWD%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## How to Contribute

Contribution and feedback are encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors

The German government has asked SAP and Deutsche Telekom to develop the Corona-Warn-App for Germany as open source software. Deutsche Telekom is providing the network and mobile technology and will operate and run the backend for the app in a safe, scalable and stable manner. SAP is responsible for the app development, its framework and the underlying platform. Therefore, development teams of SAP and Deutsche Telekom are contributing to this project. At the same time our commitment to open source means that we are enabling -in fact encouraging- all interested parties to contribute and become part of its developer community.

## Repositories

The following public repositories are currently available for the Corona-Warn-App:

| Repository          | Description                                                           |
| ------------------- | --------------------------------------------------------------------- |
| [cwa-documentation] | Project overview, general documentation, and white papers             |
| [cwa-server]        | Backend implementation for the Apple/Google exposure notification API |
| [cwa-verification-server] | Backend implementation of the verification process|

[cwa-documentation]: https://github.com/corona-warn-app/cwa-documentation
[cwa-server]: https://github.com/corona-warn-app/cwa-server
[cwa-verification-server]: https://github.com/corona-warn-app/cwa-verification-server

## Licensing

Copyright (c) 2020 Deutsche Telekom AG.

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.
