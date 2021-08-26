FROM azul/zulu-openjdk-alpine:13.0.8-jre-headless
WORKDIR /app

LABEL org.opencontainers.image.source="https://github.com/Blad3Mak3r/Killjoy"

RUN apk upgrade --no-cache

COPY docker/entrypoint.sh entrypoint.sh
COPY build/libs/Killjoy.jar Killjoy.jar

RUN chmod +x entrypoint.sh && \
    chmod +x Killjoy.jar

ENV KILLJOY_JVM_OPTIONS="-Xmx1G -XX:+UseG1GC"

ENTRYPOINT ["./entrypoint.sh"]
