FROM azul/zulu-openjdk-alpine:13.0.8-jre-headless

LABEL org.opencontainers.image.source="https://github.com/Blad3Mak3r/Killjoy"

# Update and upgrade Alpine
RUN apk --no-cache update && apk --no-cache upgrade
RUN rm -vrf /var/cache/apk/*

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
