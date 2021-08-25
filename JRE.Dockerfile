FROM alpine:latest

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN wget --quiet https://cdn.azul.com/public_keys/alpine-signing@azul.com-5d5dc44c.rsa.pub -P /etc/apk/keys/ && \
    echo "https://repos.azul.com/zulu/alpine" >> /etc/apk/repositories && \
    apk --no-cache add zulu13-jre && \
    apk --no-cache update && \
    apk --no-cache upgrade && \
    rm -vrf /var/cache/apk/*

ENV JAVA_HOME=/usr/lib/jvm/zulu13-ca