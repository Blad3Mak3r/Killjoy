FROM azul/zulu-openjdk-alpine:13.0.8-jre-headless
LABEL org.opencontainers.image.source="https://github.com/Blad3Mak3r/Killjoy"

RUN apk upgrade --no-cache

COPY --chmod=+x docker/entrypoint.sh entrypoint.sh
COPY --chmod=+x build/libs/Killjoy.jar Killjoy.jar

ENV JAVA_OPTIONS="-Xmx1G -XX:+UseG1GC"

ENTRYPOINT ["./entrypoint.sh"]
