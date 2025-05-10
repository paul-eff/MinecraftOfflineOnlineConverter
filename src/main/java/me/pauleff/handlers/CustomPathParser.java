package me.pauleff.handlers;

import me.pauleff.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for parsing custom paths from a YAML configuration file.
 * Handles recursive directory listing and file filtering.
 *
 * @author Paul Ferlitz
 */
public class CustomPathParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPathParser.class);

    private final String baseDirectory;
    private final Path pathFile;

    /**
     * Initializes the parser and verifies the custom_paths.yml file.
     *
     * @param baseDirectory The base directory for path resolution.
     */
    public CustomPathParser(String baseDirectory) {
        this.baseDirectory = baseDirectory;
        Path temp;
        try {
            temp = Path.of("custom_paths.yml");
            if (!Files.exists(temp)) {
                throw new InvalidPathException("custom_paths.yml", "Custom Paths file was not found");
            }
        } catch (InvalidPathException e) {
            temp = null;
            LOGGER.warn("Custom Paths file (custom_paths.yml) not found, continuing without!");
        }
        this.pathFile = temp;
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
     * Parses and retrieves file paths from the YAML configuration.
     *
     * @return A list of file paths extracted from YAML.
     */
    public List<String> getPaths() {
        List<String> pathList = new ArrayList<>();
        if (this.pathFile == null) {
            return pathList;
        }

        try (Reader reader = Files.newBufferedReader(this.pathFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(reader);

            if (yamlData == null || !yamlData.containsKey("paths")) {
                LOGGER.warn("No valid paths found in YAML configuration.");
                return pathList;
            }

            if (yamlData.containsKey("config")) {
                Map<String, Object> config = (Map<String, Object>) yamlData.get("config");
                LOGGER.info("Config version: {}", config.get("version"));
            }

            List<Map<String, Object>> pathsMap = (List<Map<String, Object>>) yamlData.get("paths");
            for (Map<String, Object> path : pathsMap) {
                String filePath = (String) path.get("path");
                boolean isRecursive = (boolean) path.getOrDefault("recursive", false);

                if ("folder".equals(path.get("type"))) {
                    if (isRecursive) {
                        pathList.addAll(getPathsRecursively(filePath));
                        if (Main.getArgs().hasOption("v")) LOGGER.info("Recursively added folder: {}", filePath);
                    } else {
                        pathList.addAll(getFolderContent(filePath));
                        if (Main.getArgs().hasOption("v")) LOGGER.info("Added folder: {}", filePath);
                    }
                } else {
                    pathList.add(filePath);
                    if (Main.getArgs().hasOption("v")) LOGGER.info("Added file: {}", filePath);
                }
                //System.out.println("Type: " + path.get("type"));
                //System.out.println("Path: " + path.get("path"));
                //if (path.containsKey("recursive")) System.out.println("Recursive: " + path.get("recursive"));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read custom_paths.yml", e);
            throw new RuntimeException("Error reading custom_paths.yml", e);
        }
        return pathList;
    }

    /**
     * Recursively retrieves file paths from a base folder.
     *
     * @param baseFolder The folder to scan.
     * @return A list of file paths within the folder and subfolders.
     */
    public List<String> getPathsRecursively(String baseFolder) {
        try {
            Path path = Path.of(this.baseDirectory, baseFolder);
            return Files.walk(path)
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error while recursively fetching paths from: {}", baseFolder, e);
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
        Path path = Path.of(this.baseDirectory, baseFolder);
        File[] files = path.toFile().listFiles(File::isFile);

        if (files == null) {
            LOGGER.warn("Failed to list contents of folder: {}", baseFolder);
            return Collections.emptyList();
        }

        return Arrays.stream(files).map(File::toString).collect(Collectors.toList());
    }
}
