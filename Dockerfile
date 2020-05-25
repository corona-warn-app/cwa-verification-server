FROM maven:3.6.3-jdk-11 as build

COPY . /build/
WORKDIR /build

RUN mkdir -p /root/.m2 /usr/tsi
RUN mvn --batch-mode ${MAVEN_ARGS} install
RUN cp target/*.jar /usr/tsi/app.jar

FROM gcr.io/distroless/java:11
COPY --from=build /usr/tsi/app.jar .
CMD ["app.jar"]
