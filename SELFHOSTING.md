# Self-hosting

**Prerequisites:**
- [Discord Application Token][devs_application]
- [Java 11](https://openjdk.java.net/projects/jdk/11/)
- [Docker](https://www.docker.com/) (optional)
- [Riot Games Production API Key](https://developer.riotgames.com/app-type)

To make KillJOY connect to discord you need your Discord app login token, you can get it [here][devs_application] by creating a new Bot Application.

This token has to be passed to KILLJOY through a configuration file called [**credentials.conf**](/credentials.conf.example).

Copy the content of [``credentials.conf.example``](credentials.conf.example) to ``credentials.conf`` where the Killjoy executable is placed.
```shell
java -jar Killjoy.jar
```

## Usind Docker
Update your ``credentials.conf``:
```hocon
database {
  synchronized: true
  host: killjoy_db
  port: 5432
  name: YOUR_DBNAME
  user: YOUR_USERNAME
  password: YOUR_PASSWORD
}

redis {
  host: killjoy_cache
  port: 6379
  pass: YOUR_PASS
}
```

Create a file called ``docker-compose.yml``:

```yml
version: "3.9"

services:
  cache:
    image: redis:alpine
    hostname: killjoy_cache
    restart: on-failure
    healthcheck:
      test: [ "CMD", "redis-cli","ping" ]
    expose:
      - 6379
    volumes:
      - killjoy-cache:/data

  db:
    image: postgres:alpine
    hostname: killjoy_db
    restart: on-failure
    expose:
      - 5432
    environment:
      POSTGRES_USER: killjoy
      POSTGRES_PASSWORD: killjoy
      POSTGRES_DB: killjoy
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready" ]
      interval: 10s
      timeout: 5s
      retries: 5
    volumes:
      - killjoy-data:/var/lib/postgresql/data

  bot:
    image: blademaker/killjoy
    volumes:
      - ./credentials.conf:/app/credentials.conf:ro
      - killjoy-logs:/app/logs
    restart: on-failure
    ports:
      - "8080:8080"
    links:
      - db
    depends_on:
      cache:
        condition: service_healthy
      db:
        condition: service_healthy
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 128M

volumes:
  killjoy-logs:
    external: true
  killjoy-data: { }
  killjoy-cache: { }
```

Now run ``docker-compose up -d``

### SystemD (Linux)

Create **Killjoy** user and download **KilljoyAI.jar**.
```shell
$ adduser killjoy
$ mkdir /opt/killjoy
$ chown killjoy:killjoy /opt/killjoy
$ cd /opt/killjoy
$ wget https://github.com/Blad3Mak3r/KILLJOY/releases/download/0.14/Killjoy.jar
```

Create a ``killjoy.service`` file in ``/etc/systemd/system``
```shell
$ nano /etc/systemd/system/killjoy.service
```
```shell
[Unit]
Description=Killjoy Service
After=network.target

[Service]
Type=simple

User=killjoy
Group=killjoy

WorkingDirectory=/opt/killjoy
ExecStart=/usr/bin/java -jar Killjoy.jar

TimeoutStopSec=10
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Copy the content of [``credentials.conf.example``](credentials.conf.example) to ``credentials.conf`` where the Killjoy executable is placed.

```shell
$ systemctl start killjoy.service
$ systemctl enable killjoy.service
```

[devs_application]: https://discord.com/developers/applications