FROM eclipse-temurin:17-jre
WORKDIR /app
ARG JAR_FILE=target/management-0.0.1.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]