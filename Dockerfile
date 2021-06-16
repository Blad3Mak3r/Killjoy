FROM gradle:7.0.2-jdk11 AS builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon -i --stacktrace

FROM azul/zulu-openjdk-alpine:13-jre
LABEL org.opencontainers.image.source="https://github.com/Blad3Mak3r/Killjoy"

# Fix vulnerability CVE-2021-31535 [libx11/libx11]
RUN apk update --no-cache && \
    apk upgrade --no-cache libx11

# Expose default Prometheus port
EXPOSE 8080

# Create app directory and use it as workdir
RUN mkdir /app
WORKDIR /app

# Copy Killjoy.jar from builder
COPY --from=builder /home/gradle/src/build/libs/Killjoy.jar Killjoy.jar

# Create Killjoy group and user for non-root execution and give permissions
RUN addgroup -g 1000 -S Killjoy && \
    adduser -u 1000 -S Killjoy -G Killjoy
RUN chown -R Killjoy:Killjoy /app && \
    chmod 755 /app && \
    chmod +rx Killjoy.jar
USER Killjoy

# Setup default JAVA_OPTIONS
ENV JAVA_OPTIONS="-Xmx1G -XX:+UseG1GC"

# Entrypoint for container execution
ENTRYPOINT java $JAVA_OPTIONS -jar Killjoy.jar