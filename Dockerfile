FROM gradle:6.7.0-jdk11 AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY build.gradle.kts /home/gradle/java-code/
WORKDIR /home/gradle/java-code
RUN gradle clean build -i --stacktrace

FROM gradle:6.7.0-jdk11 AS builder
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /usr/src/java-code/
WORKDIR /usr/src/java-code
RUN gradle shadowJar -i --stacktrace

FROM azul/zulu-openjdk-alpine:11
EXPOSE 8080
RUN mkdir /app
COPY --from=builder /usr/src/java-code/build/libs/KilljoyAI.jar /app/KilljoyAI.jar
WORKDIR /app
ENTRYPOINT ["java", "-Xmx4G", "-jar", "KilljoyAI.jar"]