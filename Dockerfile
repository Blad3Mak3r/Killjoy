FROM gradle:7.0.2-jdk11 AS builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon -i --stacktrace

FROM azul/zulu-openjdk-alpine:13-jre
LABEL org.opencontainers.image.source="https://github.com/Blad3Mak3r/Killjoy"
EXPOSE 8080
RUN mkdir /app
WORKDIR /app
COPY --from=builder /home/gradle/src/build/libs/Killjoy.jar Killjoy.jar
ENV JAVA_OPTIONS="-Xmx1G -XX:+UseG1GC"
ENTRYPOINT java $JAVA_OPTIONS -jar Killjoy.jar
