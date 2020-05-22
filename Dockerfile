FROM maven:3.6.3-jdk-11 as build

ARG WORK_DIR=/build

COPY . ${WORK_DIR}/
WORKDIR ${WORK_DIR}

RUN mkdir -p /root/.m2 /usr/tsi/verification-server
RUN cd ${WORK_DIR}
RUN mvn -B ${MAVEN_ARGS} install
RUN cp ${WORK_DIR}/target/cwa-verification-server*.jar /usr/tsi/verification-server/verification.jar

FROM gcr.io/distroless/java:11
COPY --from=build /usr/tsi/verification-server/verification.jar .
CMD ["verification.jar"]
EXPOSE 8080
LABEL Version="0.3-alpha"
LABEL Name="cwa-verification-server"
