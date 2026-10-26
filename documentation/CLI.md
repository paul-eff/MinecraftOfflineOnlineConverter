# CLI guide

See the [README](../README.md#cli-arguments) for all flags. Stop the server and back up affected
files first. Replace the JAR filename in these examples with the one you downloaded.

`-p` selects the server folder and defaults to the terminal's current directory, wherever the
JAR is stored. Quote paths containing spaces.

## Convert a separate folder or single file

Example in [Issue #24](https://github.com/paul-eff/MinecraftOfflineOnlineConverter/issues/24).

Convert a plugin's data folder recursively:

```bash
java -jar MinecraftOfflineOnlineConverter.jar -p "/path/to/MyServer" -offline -customFolder "plugins/ExamplePlugin/playerdata"
```

For one file, replace `-customFolder` with `-customFile "path/to/file.json"`. Both accept absolute
paths or paths relative to the server folder. They require `-online` or `-offline` and cannot be
combined with each other, `-copy`, or `-properties`. A complete world or `server.properties` is
not required.

**Warning:** Online conversion still needs the server folder's `usercache.json`, even for a
separate target. Unresolved players are skipped. If no players resolve, nothing is changed.

### Include server files with a partial conversion

Add `-includeServerFiles` to update the server configuration, profile caches, whitelist,
operator list, and ban lists alongside your target:

```bash
java -jar MinecraftOfflineOnlineConverter.jar -p "/path/to/MyServer" -offline -customFolder "world/playerdata" -includeServerFiles
```

**Warning:** Without this flag, files outside your target keep their previous state. The server
configuration and converted files can drift apart, causing missing inventories or progress and
incorrect permissions or access checks. You must manually align `online-mode` and UUID references
in the remaining server, world, and plugin files before restarting. Changing `online-mode` alone
does not convert player data.

The flag requires a custom target and does not convert the rest of the world or other plugin
data. Check that those files also match the intended mode. When repairing an isolated file for
a server already using that mode, keep the existing server configuration.

## Use a custom profile API

Example in [Issue #23](https://github.com/paul-eff/MinecraftOfflineOnlineConverter/issues/23).

Use `-customApiBaseUrl "https://profiles.example.com"` for a Mojang-compatible profile service.
If the service uses different lookup paths, specify its endpoint URLs instead:

```bash
java -jar MinecraftOfflineOnlineConverter.jar -p "/path/to/MyServer" -online -retrieveUUIDUrl "https://profiles.example.com/api/by-name/" -retrieveNameUrl "https://profiles.example.com/api/by-uuid/"
```

MOOC appends the player name or UUID to each endpoint URL. Leave placeholders out of the URL.
Each endpoint overrides `-customApiBaseUrl` for that lookup only.

**Note:** These flags only change profile lookups. They do not configure server authentication
or skins. The service must return Mojang-compatible profiles.

## Copy player data into a replacement world

Exmaple in [Pull Request #23](https://github.com/paul-eff/MinecraftOfflineOnlineConverter/pull/18).

Place the old world at `/path/to/MyServer/old-world`, then copy its player data into the active
world selected by `level-name` in `server.properties`:

```bash
java -jar MinecraftOfflineOnlineConverter.jar -p "/path/to/MyServer" -copy "old-world"
```

Both worlds must exist and use compatible Minecraft versions and dimension layouts. Use a
source folder name relative to the server root. Confirm the copy when prompted.

**Warning:** Matching destination files are overwritten. Copying does not change UUID mode or
transfer pets. Existing players keep their destination location and spawn settings where
available. New player files retain source locations, so check that players will spawn safely.

## Edit server properties

Set several properties at once. Quote the whole pair when a value contains spaces:

```bash
java -jar MinecraftOfflineOnlineConverter.jar -p "/path/to/MyServer" -properties difficulty=hard "motd=Friends' server"
```

This requires `server.properties` and the active world, but no conversion mode.

**Warning:** Property edits are applied without a confirmation prompt. Use `-online` or
`-offline` to switch UUID mode rather than only changing the `online-mode` property.
