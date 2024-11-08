# MinecraftOfflineOnlineConverter v2.3.4
<p align="center">
  <img src="https://img.shields.io/badge/version-2.3.4-blue">
  <img src="https://img.shields.io/badge/minecraft-1.21.3-green">
  <img src="https://img.shields.io/badge/java-20-red">
</p>

Ever started playing on one of your Minecraft servers and a few hours later that one friend that didn't buy the game wants to join?
Now you have to start a lengthy process of turning your server to offline mode and making sure everyone has their inventory and achievements after the switch... 

This happened to me way to often, and now I automated the whole process.
The Java applicatin will convert all player related files to be offline or online compatible.

# Supporting

Currently I tested and can confirm support for Vanilla, Bukkit, Paper, Purpur, Spigot and Forge servers.

Every Minecraft version since and including 1.17.1 is confirmed to work. Earlier versions I can't guarantee.

NOTE: Not tested yet, but Minecraft versions 1.21.1 - 1.21.3 should also be fully supported!

# Usage

- Obviously download the jar
- Place it in your server's main folder (not mandatory, just makes things easier)
- Execute the jar through your terminal with the following command:
```java
java -jar MinecraftOfflineOnlineConverter_2_3_4.jar <arguments>
```
- You will have to pass one of these arguments
  - `-offline` to convert your server to offline files
  - `-online` to convert your server to online files
- And if necessary `-p "path/to/server/folder/"` (if the jar is not in the server's main folder)

# Building

Execute the command 
```zsh
mvn clean compile assembly:single
```
to build a JAR file of the project with all dependencies.

# Disclaimer

Please always make a backup of your game files before using this tool.
Whilst it was thoroughly tested on my own servers, there is always the chance that a bug might occur!

If you need this application for a lower Minecraft and/or Java version, please leave me a message or issue :)!

# Remark

Minecraft is a registered trademark of Mojang AB.
