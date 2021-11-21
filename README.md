# MinecraftOfflineOnlineConverter v2.2.0
<p align="center">
  <img src="https://img.shields.io/badge/version-2.2.0-blue">
  <img src="https://img.shields.io/badge/minecraft-1.17.1-green">
  <img src="https://img.shields.io/badge/java-16.0.1-red">
</p>

Ever started playing on one of your Minecraft servers and a few hours later that one friend that didn't buy the game wants to join?
Well it happened to me way to often, and now I automated it.
This little Java applicatin will convert all player related files in your world folder to be offline or online compatible.

# Usage

- Obviously download the jar
- Place it in your server's main folder (not mandatory, just makes things easier)
- Execute the jar through your terminal with the following command:
```java
java -jar MinecraftOfflineOnlineConverter_2_2_0.jar <arguments>
```
- You will also have to pass one of these arguments
  - `-offline` to convert your server to offline files
  - `-online` to convert your server to online files
- And if necessary `-p "path/to/server/folder/"` (if the jar is not in the server's main folder)

# Disclaimer

Please do always make a backup of your game files before using this tool.
Whilst it was thoroughly tested on my own servers, there is always the chance that a bug might occur!

# TODO

- JavaDoc the whole project
- Make version with GUI

# Remark

Minecraft is a registered trademark of Mojang AB.
