FROM maven:3.6.3-jdk-11 as build

ARG WORK_DIR=/.

COPY . ${WORK_DIR}/
WORKDIR ${WORK_DIR}

RUN mkdir -p /root/.m2 /usr/tsi/verification-service
RUN cd ${WORK_DIR}
RUN mvn -B -DskipTests=true ${MAVEN_ARGS} clean install
RUN cp ${WORK_DIR}/services/verification-service/target/verification-service-*.jar /usr/tsi/verification-service

FROM gcr.io/distroless/java:11
COPY --from=build /usr/tsi/verification-service/verification-service*.jar .
CMD ["verification-service-0.0.1-SNAPSHOT.jar"]
