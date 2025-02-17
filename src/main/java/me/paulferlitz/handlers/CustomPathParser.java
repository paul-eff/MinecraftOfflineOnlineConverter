package me.paulferlitz.handlers;

import me.paulferlitz.Main;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
This class sucks, I know, future me will fix it sometime in the future
 */
public class CustomPathParser
{
    private final String baseDirectory;
    private final Path pathFile;

    public CustomPathParser(String baseDirectory)
    {

        this.baseDirectory = baseDirectory;
        Path temp;
        try{
            temp = Path.of("custom_paths.yml");
            // This is also shit, I know, didn't feel like throwing 2 separate exceptions...
            if (!temp.toFile().exists()) throw new InvalidPathException("custom_paths.yml", "Custom Paths file was not found");
        }catch (InvalidPathException e)
        {
            temp = null;
            System.out.println("Custom Paths file (custom_paths.yml) not found, continuing without!");
        }
        this.pathFile = temp;
    }

    public boolean isFileSet()
    {
        return (this.pathFile != null);
    }

    public ArrayList<String> getPaths()
    {
        ArrayList<String> pathList = new ArrayList<>();

        try (Reader reader = new FileReader(this.pathFile.toString()))
        {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(reader);

            for (Map.Entry<String, Object> topLevelEntry : yamlData.entrySet())
            {
                if (topLevelEntry.getKey().equals("config"))
                {
                    Map<String, Object> temp = (Map<String, Object>) topLevelEntry.getValue();
                    System.out.println("Config version: " + temp.get("version"));
                } else if (topLevelEntry.getKey().equals("paths"))
                {
                    if (topLevelEntry.getValue() instanceof List<?>)
                    {
                        List<Map<String, Object>> pathsMap = (List<Map<String, Object>>) topLevelEntry.getValue();
                        for (Map<String, Object> path : pathsMap)
                        {
                            if (path.get("type").equals("folder"))
                            {
                                if ((boolean)path.getOrDefault("recursive", false))
                                {
                                    pathList.addAll(getPathsRecursively((String) path.get("path")));
                                    if(Main.getArgs().hasOption("v")) System.out.println("Recursively added folder: " + path.get("path"));
                                } else
                                {
                                    pathList.addAll(getFolderContent((String) path.get("path")));
                                    if(Main.getArgs().hasOption("v")) System.out.println("Added folder: " + path.get("path"));
                                }
                            }else
                            {
                                pathList.add((String) path.get("path"));
                                if(Main.getArgs().hasOption("v")) System.out.println("Added file: " + path.get("path"));
                            }
                            //System.out.println("Type: " + path.get("type"));
                            //System.out.println("Path: " + path.get("path"));
                            //if (path.containsKey("recursive")) System.out.println("Recursive: " + path.get("recursive"));
                        }
                    }
                }
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return pathList;
    }

    // LEFT OFF HERE - THIS JUST RETURN FOLDER. MAKE IT RETURN FILES AS THIS IS THE NEW WAY

    public ArrayList<String> getPathsRecursively(String baseFolder)
    {
        ArrayList<String> fileList = new ArrayList<>();

        try
        {
            Path path = Path.of(this.baseDirectory)
                    .resolve(baseFolder)
                    .resolve("./");
            List<Path> list = Files.walk(path)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path folder : list)
            {
                fileList.add(folder.toString());
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            //folderList.clear();
        }

        return fileList;
    }

    public ArrayList<String> getFolderContent(String baseFolder)
    {
        ArrayList<String> fileList = new ArrayList<>();

        Path path = Path.of(this.baseDirectory)
                .resolve(baseFolder)
                .resolve("./");
        File[] files = path.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                fileList.add(file.toPath().toString());
            }
        }

        return fileList;
    }
}
