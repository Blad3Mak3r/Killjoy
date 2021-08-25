FROM blademaker/jre:latest

LABEL org.opencontainers.image.source="https://github.com/Blad3Mak3r/Killjoy"
LABEL org.opencontainers.image.authors="Juan Luis Caro Benito <blademaker.live@gmail.com>"

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
