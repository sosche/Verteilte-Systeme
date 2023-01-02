##
## Build stage
##

FROM maven:3.8.1-openjdk-11-slim AS build
LABEL maintainer = kuch_scheraz
LABEL stage = build

COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package


##
## Run stage
##
FROM openjdk:11.0-jre-slim
LABEL maintainer = kuch_scheraz
COPY --from=build /tmp/target/Coal-1.0-SNAPSHOT-jar-with-dependencies.jar /app/Coal.jar
WORKDIR /app
CMD [ "java", "-jar", "/app/Coal.jar" ]
