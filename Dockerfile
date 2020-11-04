FROM gradle:6.7.0-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM azul/zulu-openjdk-alpine:11
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/KilljoyAI.jar /app/KilljoyAI.jar
WORKDIR /app
ENTRYPOINT ["java", "-Xmx4G", "-jar", "KilljoyAI.jar"]