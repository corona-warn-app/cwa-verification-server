FROM gcr.io/distroless/java-debian10:11
WORKDIR /
COPY target/*.jar app.jar
COPY scripts/Dpkg.java Dpkg.java
RUN ["java", "Dpkg.java"]
USER 65534:65534
CMD ["app.jar"]
EXPOSE 8080
