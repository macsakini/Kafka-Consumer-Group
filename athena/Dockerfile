FROM gradle:7.6.0-jdk11-alpine

COPY build.gradle .

COPY src src

RUN ./gradlew build