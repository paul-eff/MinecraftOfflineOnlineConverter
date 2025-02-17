# MinecraftOfflineOnlineConverter Beta v4
<p align="center">
  <img src="https://img.shields.io/badge/release-Beta-blue">
  <img src="https://img.shields.io/badge/version-3-blue">
  <img src="https://img.shields.io/badge/minecraft-1.21.4-green">
  <img src="https://img.shields.io/badge/java-21-red">
</p>

Ever started playing on one of your Minecraft servers and a few hours later that one friend that didn't buy the game wants to join?
Now you have to start a lengthy process of turning your server to offline mode and making sure everyone has their inventory and achievements after the switch... 

This happened to me way to often, and now I automated the whole process.
The Java applicatin will convert all player related files to be offline or online compatible.

# Usage

This is a pre-release, make backups! See [here](#disclaimer)

- Obviously download the jar
- Place it in your server's main folder (not mandatory, just makes things easier)

- Optional: Rename `TEMPLATE.custom_paths.yml` to `custom_paths.yml` and use the examples presented to define custom directories you want to convert

- Execute the jar through your terminal with the following command:
```java
java -jar MinecraftOfflineOnlineConverter_3-BETA_2.jar <arguments>
```
- You will have to pass one of these arguments
  - `-offline` to convert your server to offline files
  - `-online` to convert your server to online files
- If necessary `-p "path/to/server/folder/"` (if the jar is not in the server's main folder)
- `-v` for verbose output

## Known Issues

- On Paper servers, when converting to offline and forgetting to change the `server.properties` accordingly.
  Paper creates new files (e.g. `<Online UUID>.dat.offline-read`). This can later on lead to inconsistencies.
- No support for Sponge servers.

## TODOs

- Ensure anything to do with files and directories is not done via Strings - done..ish
- Cleanup spaghetti code
- Cleanup outputs/logging

## Building

Execute the command 
```zsh
mvn clean compile assembly:single
```
to build a JAR file of the project with all dependencies.

# Disclaimer

Please always make a backup of your game files before using this tool.
Whilst it was thoroughly tested on my own servers, there is always the chance that a bug might occur!

If you need this application for a lower Minecraft and/or Java version, please leave me a message or issue :)!

# License

This project is licensed under the [MIT License](LICENSE).

# Remark

Minecraft is a registered trademark of Mojang AB.
