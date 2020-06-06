FROM gcr.io/distroless/java:11
COPY target/*.jar app.jar
CMD ["app.jar"]
EXPOSE 8080
