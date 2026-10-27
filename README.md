# MinecraftOfflineOnlineConverter v26.2

<p align="center">
  <img src="https://img.shields.io/badge/version-26.2-blue" alt="App version 26.2">
  <img src="https://img.shields.io/badge/minecraft-26.2-green" alt="Minecraft version 26.2">
  <img src="https://img.shields.io/badge/java-25-red" alt="Java version 25">
</p>

Ever started playing on one of your Minecraft servers and a few hours later that one friend that didn't buy the game
wants to join?
Now you have to start a lengthy process of turning your server to offline mode and making sure everyone has their
inventory and achievements after the switch...
Convert Minecraft player data between online and offline mode while keeping inventories and progress.
Requires Java 25.

This happened to me way to often, and now I automated the whole process.
The Java applicatin will convert all player related files to be offline or online compatible.

## Supported Servers

| Server Type | Min. MC Version | Max. MC Version |
|:------------|:----------------|:----------------|
| Vanilla     | 1.7.6           | 26.2            |
| Bukkit      | 1.7.6           | 26.2            |
| Forge       | 1.7.10          | 26.2            |
| Fabric      | 1.14            | 26.2            |
| Paper       | 1.7.10          | 26.2            |
| Spigot      | 1.8             | 26.2            |
| NeoForge    | WIP             | WIP             |
| Sponge      | WIP             | WIP             |

Mojang introduced UUIDs in Minecraft 1.7.6 (2014). Converting between pre 1.7.6 and post 1.7.6 is currently not being
pursued and will not be any time soon.  
If you find any bugs or edge cases, please report them to this repo!

## Usage

Stop the server and back up its files. Read the [disclaimer](#disclaimer) before continuing.
Download the [latest JAR](https://github.com/paul-eff/MinecraftOfflineOnlineConverter/releases/latest),
then run this command from your server folder:

```bash
java -jar MinecraftOfflineOnlineConverter.jar -offline
```

Replace the JAR filename with the one you downloaded. Use `-online` for online mode or
`-p "/path/to/MyServer"` to select another server folder. Review the settings and confirm when prompted.

### CLI Arguments

| Argument                      | Description                                                             |
|:------------------------------|:------------------------------------------------------------------------|
| `-offline`                    | Convert to offline mode.                                                |
| `-online`                     | Convert to online mode.                                                 |
| `-p <path>`, `--path <path>`  | Server folder (default: current terminal directory).                    |
| `-customFolder <path>`        | Convert only this folder recursively. Use with `-online` or `-offline`. |
| `-customFile <path>`          | Convert only this file. Use with `-online` or `-offline`.               |
| `-includeServerFiles`         | Also convert server files. Use with a custom target.                    |
| `-copy <world-name>`          | Copy player data from this world to the active world.                   |
| `-properties key=value [...]` | Edit `server.properties` values.                                        |
| `-customApiBaseUrl <url>`     | Base URL for Mojang-compatible profile lookups.                         |
| `-retrieveUUIDUrl <url>`      | Override the base URL for name-to-UUID lookups.                         |
| `-retrieveNameUrl <url>`      | Override the base URL for UUID-to-name lookups.                         |
| `-verbose`                    | Enable debug console output.                                            |
| `-v`                          | Print MOOC version.                                                     |
| `-h`, `--help`                | Show help.                                                              |

See the [CLI guide](documentation/CLI.md) for explanations and real examples for using custom targets, profile APIs,
player data copying, and property edits.

**Warning:** With `-customFolder` or `-customFile`, use `-includeServerFiles` or manually keep server
configuration and converted files in sync to avoid errors. See the
[partial conversion warning](documentation/CLI.md#include-server-files-with-a-partial-conversion).

Online conversion needs the server folder's `usercache.json`. Unresolved players are skipped.
If no players resolve, nothing is changed.

## Known Issues

- Paper may create `<Online UUID>.dat.offline-read` files after offline conversion.
- Entity relations tied to players (e.g. pet ownership) is not
  transferred. [Here is a workaround](https://www.reddit.com/r/Minecraft/comments/9bmthx/change_pet_ownership_with_command_using_uuid_in/)
  that works for now
- Currently, there is no good way to support niche/mod specific use cases
  (e.g. [ConvertFtbQuests.java](src/main/java/me/pauleff/converter/plugins/ConvertFtbQuests.java)
  in [Issue #19](https://github.com/paul-eff/MinecraftOfflineOnlineConverter/issues/19)).
  The plugin system is designed for broader conversions and also keeping these "scripts" up-to-date can turn into a
  burden very quickly. I will keep thinking about possible solutions
  but for now I will not further pursue it.

## Building

Build a runnable JAR with all dependencies:

```bash
./gradlew clean shadowJar
```

## Disclaimer

Please always make a backup of your game files before using this tool.
Whilst it was thoroughly tested on my own servers, there is always the chance that a bug might occur!

## License

Licensed under the [MIT License](LICENSE).

Minecraft is a registered trademark of Mojang AB.
