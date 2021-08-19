FROM azul/zulu-openjdk-alpine:13.0.7-jre-headless
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
COPY build/libs/Killjoy.jar Killjoy.jar

# Setup default JAVA_OPTIONS
ENV JAVA_OPTIONS="-Xmx1G -XX:+UseG1GC"
ENV KILLJOY_ARGS=""

# Entrypoint for container execution
ENTRYPOINT java $JAVA_OPTIONS -jar Killjoy.jar $KILLJOY_ARGS
