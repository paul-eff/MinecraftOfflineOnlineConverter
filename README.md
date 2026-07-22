# MinecraftOfflineOnlineConverter v26.1
<p align="center">
  <img src="https://img.shields.io/badge/version-26.1-blue" alt="App version 26.1">
  <img src="https://img.shields.io/badge/minecraft-26.2-green" alt="Minecraft version 26.2">
  <img src="https://img.shields.io/badge/java-26-red" alt="Java version 26">
</p>

Ever started playing on one of your Minecraft servers and a few hours later that one friend that didn't buy the game wants to join?
Now you have to start a lengthy process of turning your server to offline mode and making sure everyone has their inventory and achievements after the switch... 

This happened to me way to often, and now I automated the whole process.
The Java applicatin will convert all player related files to be offline or online compatible.

# Supporting

| Server Type | Min. MC Version | Max. MC Version |
|:------------|:----------------|:----------------|
| Vanilla     | Beta 1.8.1      | 26.2            |
| Bukkit      | Beta 1.8.1      | 26.2            |
| Forge       | 1.7.10          | 26.2            |
| Fabric      | 1.14            | 26.2            |
| Paper       | 1.7.10          | 26.2            |
| Spigot      | 1.8             | 26.2            |

Mojang introduced UUIDs in Minecraft 1.7.6 (2014) to allow name changing. Therefor, if your server is older than that,
you will not need this tool!  
If you find any bugs or edge cases, please report them to this repo!

# Usage

First look at [Disclaimer](#disclaimer) and make sure you understand the "risks" of using this tool.

- Download the [most current jar](https://github.com/paul-eff/MinecraftOfflineOnlineConverter/releases/latest)
- Place it in your server's main folder (not mandatory, just makes things easier)

- Execute the jar through your terminal with the following command:

```bash
java -jar MinecraftOfflineOnlineConverter.jar <arguments>
```

- `-offline` to convert your server to offline files
- `-online` to convert your server to online files
- If necessary `-p "path/to/server/folder/"` (if the jar is not in the server's main folder)
- `-copy` to copy player data from one world to another
- `-properties` to directly edit values in server.properties
- `-customApiBaseUrl "https://myskinserver.com"` custom API base URL for online UUID/name lookups (Mojang API path
  schema was to be used [read more here](https://minecraft.wiki/w/Mojang_API)). Useful for custom auth/skin servers
  like [Blessing Skin](https://github.com/bs-community)
- `-retrieveUUIDUrl "https://myskinserver.com/api/.../"` full name to UUID endpoint URL (domain + path). Only the player
  name is appended. Overrides `-customApiBaseUrl` for this lookup
- `-retrieveNameUrl "https://myskinserver.com/api/.../"` full UUID to name endpoint URL (domain + path). Only the UUID
  is appended. Overrides `-customApiBaseUrl` for this lookup
- `-verbose` for verbose console output (for debugging and error reporting)
- `-v` print MOOC version
- `-h` for help

## Known Issues

- Paper servers (when converted to offline), sometimes create `<Online UUID>.dat.offline-read`) files
- No confirmed support for Sponge servers

## Building

Execute the command 
```bash
./gradlew clean fatJar
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
