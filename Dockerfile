FROM gradle:6.7.0-jdk11 AS builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle --no-daemon shadowJar -i --stacktrace

FROM azul/zulu-openjdk-alpine:11
EXPOSE 8080
RUN mkdir /app
COPY --from=builder /home/gradle/src/build/libs/KilljoyAI.jar /app/KilljoyAI.jar
WORKDIR /app
ENTRYPOINT ["java", "-Xmx4G", "-jar", "KilljoyAI.jar"]