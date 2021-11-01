# MinecraftOfflineOnlineConverter v1.1.0

Ever started playing on one of your Minecraft servers and a few hours later that one friend that didn't buy the game wants to join?
Well it happened to me way to often, and now I automated it.
This little Java applicatin will convert all player related files in your world folder to be offline compatible.

NOTE: Atm, when given no path, the app will only convert the `world` folder. You will have to pass it the path manually if you have a custom world name.

# Usage

- Obviously download the jar
- Place it in your server's main folder (not mandatory, just makes things easier)
- Execute the jar through your terminal with the following command:
```java
java -jar MinecraftOfflineOnlineConverter_1_1_0.jar <arguments>
```
- You will also have to pass one of these arguments
  - `-offline` to convert your server to offline files
  - `-online` to convert your server to online files (WIP!)
- And if necessary `-p "path/to/world/folder/"` if you want to target a specific world folder (or the jar is not in the server's main folder)

# TODO

- Make the conversion from offline to online possible
- Query server.properties to always target correct world folder
- A bit more error handling
- Make version with GUI

# Remark

Minecraft is a registered trademark of Mojang AB.
