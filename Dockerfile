FROM azul/zulu-openjdk-alpine:13.0.8-jre-headless
WORKDIR /app

LABEL org.opencontainers.image.source="https://github.com/Blad3Mak3r/Killjoy"

# Update and upgrade Alpine
RUN apk --no-cache update && apk --no-cache upgrade
RUN rm -vrf /var/cache/apk/*

# Copy Killjoy.jar from builder
COPY build/libs/Killjoy.jar Killjoy.jar

# Setup default JAVA_OPTIONS
ENV KILLJOY_JVM_OPTIONS="-Xmx1G -XX:+UseG1GC"

COPY docker/entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh

# Entrypoint for container execution
ENTRYPOINT ["./entrypoint.sh"]
