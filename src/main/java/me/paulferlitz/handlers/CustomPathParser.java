package me.paulferlitz.handlers;

import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomPathParser
{
    private final Path pathFile = Path.of("mooc_custom_paths_TESTING.yml");
    public CustomPathParser()
    {
        // Nothing
    }

    public ArrayList<String> getPaths()
    {
        ArrayList<String> pathList = new ArrayList<String>();

        try (Reader reader = new FileReader(this.pathFile.toString()))
        {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(reader);

            for (Map.Entry<String, Object> topLevelEntry : yamlData.entrySet())
            {
                if(topLevelEntry.getKey().equals("config"))
                {
                    Map<String, Object> temp = (Map<String, Object>) topLevelEntry.getValue();
                    System.out.println("Version: " + temp.get("version"));
                }
                else if(topLevelEntry.getKey().equals("paths"))
                {
                    if (topLevelEntry.getValue() instanceof List<?>)
                    {
                        List<Map<String, Object>> pathsMap = (List<Map<String, Object>>) topLevelEntry.getValue();
                        for (Map<String, Object> path : pathsMap)
                        {
                            System.out.println("Type: " + path.get("type"));
                            System.out.println("Path: " + path.get("path"));
                            pathList.add((String) path.get("path"));
                            if (path.containsKey("recursive")) System.out.println("Recursive: " + path.get("recursive"));
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
}
