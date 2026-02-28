package me.pauleff.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for parsing custom paths from a YAML configuration file.
 * Handles recursive directory listing and file filtering.
 *
 * @author Paul Ferlitz
 */
public class Config {
    private static final String CONFIG_NAME = "mooc-config.yml";
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private final Path baseDirectory;
    private final Path pathFile;
    private final List<String> pathList;
    public List<String> playerdataWorldBlacklist = new ArrayList<>();
    public List<String> playerDataTagsToKeep = new ArrayList<>();

    public List<String> getPaths() {
        return pathList;
    }

    /**
     * Constructor for CustomPathParser.
     * Initializes the base directory and checks for the existence of the custom_paths.yml file.
     *
     * @throws InvalidPathException if the provided path is invalid.
     */
    public Config(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
        pathList = new ArrayList<>();
        Path cp_path = Path.of("./" + CONFIG_NAME);

        if (Files.exists(cp_path)) {
            this.pathFile = cp_path;
        } else {
            LOGGER.warn("Config file ({}) not found, did you place it next to the MOOC.jar?", CONFIG_NAME);
            this.pathFile = null;
            return;
        }

        try (Reader reader = Files.newBufferedReader(this.pathFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(reader);
            if (yamlData == null) return;

            if (yamlData.containsKey("config")) {
                Map<String, Object> config = (Map<String, Object>) yamlData.get("config");
                LOGGER.info("Config version: {}", config.get("version"));
                playerdataWorldBlacklist = (List<String>) config.getOrDefault("playerdata-world-blacklist", new ArrayList<String>());
                playerDataTagsToKeep = (List<String>) config.getOrDefault("playerdata-tags-to-keep", new ArrayList<String>());
            }

            if (yamlData.containsKey("paths")) {
                List<Map<String, Object>> pathsMap = (List<Map<String, Object>>) yamlData.get("paths");
                if (pathsMap != null) {
                    for (Map<String, Object> path : pathsMap) {
                        String filePath = (String) path.get("path");
                        boolean isRecursive = (boolean) path.getOrDefault("recursive", false);

                        if ("folder".equals(path.get("type"))) {
                            if (isRecursive) {
                                pathList.addAll(getPathsRecursively(filePath));
                                LOGGER.debug("Recursively added folder: {}", filePath);
                            } else {
                                pathList.addAll(getFolderContent(filePath));
                                LOGGER.debug("Added folder: {}", filePath);
                            }
                        } else {
                            pathList.add(filePath);
                            LOGGER.debug("Added file: {}", filePath);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read custom_paths.yml", e);
            throw new RuntimeException("Error reading custom_paths.yml", e);
        }
    }

    /**
     * Checks whether the file is set.
     *
     * @return True if the YAML file exists, otherwise false.
     */
    public boolean isFileSet() {
        return this.pathFile != null;
    }


    /**
     * Recursively retrieves file paths from a base folder.
     *
     * @param baseFolder The folder to scan.
     * @return A list of file paths within the folder and subfolders.
     */
    public List<String> getPathsRecursively(String baseFolder) {
        // TODO: Return an array of paths or files, not strings
        Path path = this.baseDirectory.resolve(baseFolder);
        try {
            return Files.walk(path)
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error while recursively fetching paths from: {}", path.normalize(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves file paths from a non-recursive folder.
     *
     * @param baseFolder The folder to scan.
     * @return A list of file paths inside the folder.
     */
    public List<String> getFolderContent(String baseFolder) {
        // TODO: Return an array of paths or files, not strings
        Path path = this.baseDirectory.resolve(baseFolder);
        File[] files = path.toFile().listFiles(File::isFile);

        if (files == null) {
            LOGGER.warn("Failed to list contents of folder: {}", path.normalize());
            return Collections.emptyList();
        }

        return Arrays.stream(files).map(File::toString).collect(Collectors.toList());
    }
}
