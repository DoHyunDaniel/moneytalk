FROM openjdk:17-jdk-slim

ARG JAR_FILE=build/libs/moneytalk-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app.jar"]
