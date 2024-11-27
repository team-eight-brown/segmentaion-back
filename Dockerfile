FROM openjdk:23-jdk-slim

WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle /app/
COPY gradle /app/gradle
COPY src /app/src
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon
CMD ["java", "-jar", "build/libs/segmentation-0.0.1-SNAPSHOT.jar"]
