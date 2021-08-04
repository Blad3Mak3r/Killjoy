<p align="center">
    <a href="https://killjoy.dev" target="_blank"><img src="https://raw.githubusercontent.com/Blad3Mak3r/Killjoy/development/Branding/logo.svg" width="60%" alt="Killjoy Logo"></a>
</p>

## [Add to Discord][invitation]
[![TeamCity Full Build Status](https://img.shields.io/teamcity/build/s/Killjoy_Build?label=Build%20Status&server=https%3A%2F%2Fhugebot.beta.teamcity.com)](https://hugebot.beta.teamcity.com/buildConfiguration/Killjoy_Build?mode=builds&guest=1)
[![Docker Build](https://github.com/Blad3Mak3r/Killjoy/actions/workflows/docker-build-latest.yml/badge.svg)](https://github.com/Blad3Mak3r/Killjoy/actions/workflows/docker-build-latest.yml)
![GitHub top language](https://img.shields.io/github/languages/top/Blad3Mak3r/Killjoy)
![Discord](https://img.shields.io/discord/425661010662260736?logo=discord)
![Docker Image Version (latest by date)](https://img.shields.io/docker/v/blademaker/killjoy?logo=docker&sort=date)
![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/blademaker/killjoy?logo=docker&sort=date)
![Docker Pulls](https://img.shields.io/docker/pulls/blademaker/killjoy?logo=docker)
 
**Killjoy** is an open source Discord bot focused on **Valorant**.

At the moment the bot is pretty straightforward, but we have great ideas for it, you can see the **Planned Features** [here](https://github.com/Blad3Mak3r/KILLJOY/projects/1).


## Current existing commands
``[]`` Optional command arguments.

``()`` Required command arguments.


| Name                    | Description                                                                            |
| ----------------------- | -------------------------------------------------------------------------------------- |
| /**abilities** [`page`] | List all the abilities in the game.                                                    |
| /**ability** (`name`)   | Get information about an ability.                                                      |
| /**agent** (`name`)     | Get information and statistics about a Valorant agent.                                 |
| /**agents** [`page` ]   | List all the Valorant agents.                                                          |
| /**arsenal** [`weapon`] | Get information and statistics about a Valorant weapon or the entire arsenal.          |
| /**debug**              | Useful for debugging.                                                                  |
| /**help**               | Did you need help?                                                                     |
| /**invite**             | Generate an invitation link for Killjoy.                                               |
| /**maps** [`map`]       | Get a list of maps or information about a specific map from Valorant.                  |
| /**meme**               | Funny Valorant memes stolen from Reddit.                                               |
| /**news**               | The most up-to-date news from the world of VALORANT.                                   |
| /**ping**               | Check current Discord ping.                                                            |
| /**pugs close**         | Close the active PUG, requires Manage Server permissions.                              |
| /**pugs create**        | Start a new PUG on this guild, requires Manage Server permissions.                     |
| /**pugs current**       | Information about the currently active PUG on this guild.                              |
| /**pugs join**          | Join the active PUG on this guild.                                                     |
| /**pugs leave**         | Leave the active PUG on this guild.                                                    |
| /**pugs teams**         | Create two random teams of registered players, with a minimum of 4 registered players. |
| /**top** (`region`)     | Retrieve the TOP 10 players by region.                                                 |


<p align="center">
 <img alt="Example 1" src="/Branding/examples/commands_agents.png" width="400px">
</p>

<p align="center">
 <img alt="Example 2" src="/Branding/examples/commands_arsenal.png" width="400px">
</p>

# Self-hosting

**Prerequisites:**
- [Discord Application Token](devs_application)
- [Java 11](https://openjdk.java.net/projects/jdk/11/)
- [Docker](https://www.docker.com/) (optional)
- [Riot Games Production API Key](https://developer.riotgames.com/app-type)

To make KillJOY connect to discord you need your Discord app login token, you can get it [here][devs_application] by creating a new Bot Application.

This token has to be passed to KILLJOY through a configuration file called [**credentials.conf**](/credentials.conf.example).

Copy the content of [``credentials.conf.example``](credentials.conf.example) to ``credentials.conf`` where the Killjoy executable is placed.
```shell
java -jar Killjoy.jar
```

## As a service

### Docker Compose

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
```

Create a file called ``docker-compose.yml``:
```yml
version: "3.9"

services:
  db:
    image: postgres:alpine
    hostname: killjoy_db
    restart: on-failure
    expose:
      - 5432
    environment:
      POSTGRES_USERNAME: YOUR_USERNAME
      POSTGRES_PASSWORD: YOUR_PASSWORD
      POSTGRES_DB: YOUR_DBNAME
    volumes:
    - killjoy-data:/var/lib/postgresql/data
    
  bot:
    image: blademaker/killjoy
    volumes:
      - ./credentials.conf:/app/credentials.conf:ro
      - ./logs:/app/logs
    restart: on-failure
    ports:
    - 8080:8080
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 128M

volumes:
  killjoy-data: {}
  
```

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
[invitation]: https://discord.com/api/oauth2/authorize?client_id=706887214088323092&permissions=321600&scope=bot+applications.commands

## Thanks to

<img src="https://www.jetbrains.com/company/brand/img/jetbrains_logo.png" height="200" />
