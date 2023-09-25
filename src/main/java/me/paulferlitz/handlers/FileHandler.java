package me.paulferlitz.handlers;

import org.json.JSONArray;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class for handling file operations.
 *
 * @author Paul Ferlitz
 */
public class FileHandler
{
    /**
     * Method to rename a file.
     *
     * @param baseWorldFolder Base working directory.
     * @param oldFilePath     Old path / name for file in question.
     * @param newFilePath     New path / name for file in question.
     * @throws IOException If renaming wasn't possible.
     */
    public static void renameFile(String baseWorldFolder, String oldFilePath, String newFilePath) throws IOException
    {
        Path source = Paths.get(baseWorldFolder + oldFilePath);
        Path target = Paths.get(baseWorldFolder + newFilePath);
        Files.move(source, target);
    }

    /**
     * Method to list all files in a given directory.
     *
     * @param worldFolderpath Path to folder.
     * @return An array of {@link File}s.
     */
    public static File[] listAllFiles(String worldFolderpath)
    {
        File folder = new File(worldFolderpath);
        return folder.listFiles();
    }

    /**
     * Method to load the array of players in the usercache.json file.
     *
     * @param pathToUsercache Path to usercache.json file.
     * @return The usercache.json content as a {@link JSONArray}.
     */
    public static JSONArray loadArrayFromUsercache(String pathToUsercache)
    {
        String jsonString = "[]";
        try
        {
            jsonString = Files.readString(Path.of(pathToUsercache), StandardCharsets.UTF_8);
        } catch (IOException e)
        {
            System.out.println("Could not find usercache.json with given path \"" + pathToUsercache + "\"." +
                    "\nContinuing without prefetching userdata.");
        }
        return new JSONArray(jsonString);
    }

    /**
     * Method to fetch the world name from the server.properties files.
     *
     * @param pathToProperties Path to server.properties file.
     * @return The world name.
     */
    public static String readWorldNameFromProperties(String pathToProperties)
    {
        boolean foundInProperties = false;
        // Default value
        String worldName = "world";
        // Iterate over file until 'level-name' tag was found
        try (BufferedReader br = new BufferedReader(new FileReader(pathToProperties)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                if (line.startsWith("level-name="))
                {
                    worldName = line.replace("level-name=", "");
                    System.out.println("Found world name \"" + worldName + "\" in server.properties. Trying to target this world folder.");
                    foundInProperties = true;
                }
            }
        } catch (IOException e)
        {
            System.out.println("Could not find server.properties with given path \"" + pathToProperties + "\"." +
                    "\nContinuing without prefetching world name.");
        }

        if (!foundInProperties)
        {
            System.out.println("No world name found. Using default (\"" + worldName + "\").");
        }

        return worldName;
    }

    /**
     * Checks whether or not a file is a text file or a binary one.
     *
     * @param path - The file to check.
     * @return <tt>true</tt> if the File is a text file, <tt>false</tt> otherwise.
     * @throws IOException              I/O error.
     * @throws IllegalArgumentException If the file is <code>null</code> or is not a file.
     * @author <a href="http://www.java2s.com/example/java/file-path-io/checks-whether-or-not-a-file-is-a-text-file-or-a-binary-one.html">www.java2s.com/...</a>
     */
    public static boolean isText(final Path path) throws IOException, IllegalArgumentException
    {
        File file = path.toFile();

        if (!file.isFile())
            throw new IllegalArgumentException(
                    "Must not be null & must be a file.");

        try (RandomAccessFile raf = new RandomAccessFile(file, "r"))
        {
            int numberOfNonTextChars = 0;
            while (raf.getFilePointer() < raf.length())
            {
                final int b = raf.readUnsignedByte();
                // http://www.table-ascii.com/
                if (b == 0x09 || // horizontal tabulation
                        b == 0x0A || // line feed
                        b == 0x0C || // form feed
                        b == 0x0D || // carriage return
                        (b >= 0x20 && b <= 0x7E) || // "normal" characters
                        (b >= 0x80 && b <= 0x9F) || // latin-1 symbols
                        (b >= 0xA0 && b <= 0xFF)) // latin-1 symbols
                {
                    // OK
                } else
                {
                    numberOfNonTextChars++;
                }
            }
            return numberOfNonTextChars <= 2
                    && (raf.length() - (double) numberOfNonTextChars / raf.length()) >= 0.99;

        }
    }
}
