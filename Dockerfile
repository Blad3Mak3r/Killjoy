FROM gradle:7.0.2-jdk11 AS builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle --no-daemon buildBot -i --stacktrace

FROM azul/zulu-openjdk-alpine:11-jre
EXPOSE 8080
RUN mkdir /app
COPY --from=builder /home/gradle/bot/src/build/libs/Killjoy.jar /app/Killjoy.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "Killjoy.jar"]
