FROM blademaker/jre:latest
WORKDIR /app

LABEL org.opencontainers.image.source="https://github.com/Blad3Mak3r/Killjoy"

COPY docker/entrypoint.sh entrypoint.sh
COPY build/libs/Killjoy.jar Killjoy.jar

RUN chmod +x entrypoint.sh && \
    chmod +x Killjoy.jar

ENV KILLJOY_JVM_OPTIONS="-Xmx1G -XX:+UseG1GC"

ENTRYPOINT ["./entrypoint.sh"]
