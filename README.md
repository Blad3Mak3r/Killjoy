<img alt="Avatar" align="right" src="https://github.com/KilljoyBot/KilljoyAssets/raw/master/img/avatar.jpg" height="200" width="200">

# KILLJOY ([Invite][invitation])
[![TeamCity Full Build Status](https://img.shields.io/teamcity/build/s/Killjoy_Build?label=Build%20Status&server=https%3A%2F%2Fhugebot.beta.teamcity.com)](https://hugebot.beta.teamcity.com/buildConfiguration/Killjoy_Build?mode=builds&guest=1)
[![Gradle Check](https://github.com/Blad3Mak3r/Killjoy/actions/workflows/gradle.yml/badge.svg)](https://github.com/Blad3Mak3r/Killjoy/actions/workflows/gradle.yml)
![Discord](https://img.shields.io/discord/425661010662260736?logo=discord)
![Docker Image Version (latest by date)](https://img.shields.io/docker/v/blademaker/killjoy?logo=docker&sort=date)
![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/blademaker/killjoy?logo=docker&sort=date)
![Docker Pulls](https://img.shields.io/docker/pulls/blademaker/killjoy?logo=docker)
 
**Killjoy** is an open source Discord bot focused on **Valorant**.

At the moment the bot is pretty straightforward, but we have great ideas for it, you can see the **Planned Features** [here](https://github.com/Blad3Mak3r/KILLJOY/projects/1).


## Current existing commands
``[]`` Optional command arguments.

``()`` Required command arguments.

``<>`` Valid aliases for the command.


- ``joy help`` The basic help command.
- ``joy help [command]`` Help about the usage of a command.
- ``joy <agents|agent>`` Show the agent list (``joy agents`` or ``joy agent``).
- ``joy <agents|agent> [agent_name]`` Show detailed information about the agent you selected (``joy agent killjoy``).
- ``joy <agents|agent> [agent_name] (q|e|c|x)`` Show a detailed information about the agent's skill you selected (``joy agent sage x``).
- ``joy arsenal [weapon]`` Get information and statistics about a Valorant weapon or the entire aresenal.
- ``joy skills (skill_name)`` Get information and statistics about the agent's skill you selected.
- ``joy invite`` Creates a Bot invite url.
- ``joy news`` Get the latest news from [Valorant Official Website](https://playvalorant.com)
- ``joy <maps|map>`` Show the map list (``joy maps`` or ``joy map``)
- ``joy <maps|map> [map_name]`` Show detailed information about the map you selected (``joy map ascent``)
- ``joy top (region)`` Retrieve the TOP10 players of a region (``joy top eu``)

<p align="center">
 <img alt="Example 1" src="/Branding/examples/commands_agents.png" width="400px">
</p>

<p align="center">
 <img alt="Example 2" src="/Branding/examples/commands_arsenal.png" width="400px">
</p>

# Self-hosting

**Prerequisites:**
- Java JRE 11
- Docker (optional)

To make KillJOY connect to discord you need your Discord app login token, you can get it [here][devs_application] by creating a new Bot Application.

This token has to be passed to KILLJOY through a configuration file called [**killjoy.conf**](/killjoy.conf.example).

Copy the content of [``killjoy.conf.example``](killjoy.conf.example) to ``killjoy.conf`` where the Killjoy executable is placed.
```shell
java -jar KilljoyAI.jar
```

### As a service

**Docker**
```shell
docker run -it -d \
  --name=killjoy \
  --restart=always \
  --volume killjoy.conf:/app/killjoy.conf \
  blademaker/killjoy:0.8
```

**SystemD (Linux)**

Create **Killjoy** user and download **KilljoyAI.jar**.
```shell
$ adduser killjoy
$ mkdir /opt/killjoy
$ chown killjoy:killjoy /opt/killjoy
$ cd /opt/killjoy
$ wget https://github.com/Blad3Mak3r/KILLJOY/releases/download/v0.8/KilljoyAI.jar
```

Create a ``killjoy.service`` file in ``/etc/systemd/system``
```shell
$ nano /etc/systemd/system/killjoy.service
```
```shell
[Unit]
Description=KilljoyAI Service
After=network.target

[Service]
Type=simple

User=killjoy
Group=killjoy

WorkingDirectory=/opt/killjoy
ExecStart=/usr/bin/java -jar KilljoyAI.jar

TimeoutStopSec=10
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Copy the content of [``killjoy.conf.example``](killjoy.conf.example) to ``killjoy.conf`` where the Killjoy executable is placed.

```shell
$ systemctl start killjoy.service
$ systemctl enable killjoy.service
```

[devs_application]: https://discord.com/developers/applications
[invitation]: https://discord.com/api/oauth2/authorize?client_id=706887214088323092&permissions=321600&scope=bot%20applications.commands%20applications.commands.update
