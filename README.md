# MinecraftOfflineOnlineConverter v1.0.0

Ever started playing on one of your Miencraft servers and a few hours later that one friend that didn't buy the game wants to join?
Well it happened to me way to often, and now I automated it.
This little Java applicatin will convert all player related files in your world folder to be offline compatible.

NOTE: Atm, when given no path, the app will only convert the `world` folder. You will have to pass it the path manually if you have a custom world name or also want to convert `world_nether` and `world_the_end`.

# Usage

- Obviously download the jar
- Place it on your server's main folder
- Execute the jar through your terminal with the following command:
```java
java -jar MinecraftOfflineOnlineConverter.jar <arguments>
```
- You will also have to pass one of these arguments
  - `-offline` to convert your server to offline files
  - `-online` to convert your server to online files (WIP!)
  - `-p "path/to/world/folder/"` if you want to target a specific world folder (or the jar is not in the server's main folder)

# TODO

- A bit more error handling
- Make the conversion from offline to online possible
- Make version with GUI
