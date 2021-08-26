FROM azul/zulu-openjdk-alpine:13.0.8-jre-headless
LABEL org.opencontainers.image.source="https://github.com/Blad3Mak3r/Killjoy"

WORKDIR /app

ADD "https://gist.githubusercontent.com/Blad3Mak3r/2fd2f0b928e6f8484b1877dbb62566ce/raw/entrypoint.sh" entrypoint.sh
COPY build/libs/Killjoy.jar Killjoy.jar

RUN chmod +x entrypoint.sh && \
    chmod +x Killjoy.jar

ENV KILLJOY_JVM_OPTIONS="-Xmx1G -XX:+UseG1GC"

ENTRYPOINT ["./entrypoint.sh"]
