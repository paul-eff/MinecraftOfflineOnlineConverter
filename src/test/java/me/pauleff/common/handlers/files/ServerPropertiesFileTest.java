package me.pauleff.common.handlers.files;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerPropertiesFileTest
{
    @TempDir
    Path tempDir;

    private Path writeProperties(String content) throws IOException
    {
        return Files.writeString(tempDir.resolve("server.properties"), content);
    }

    @Nested
    class Of
    {
        @Test
        void returnsHandle_when_pathProvided()
        {
            Path path = tempDir.resolve("server.properties");

            ServerPropertiesFile handle = ServerPropertiesFile.of(path);

            assertEquals(path, handle.path());
        }

        @Test
        void throws_when_pathIsNull()
        {
            assertThrows(NullPointerException.class, () -> ServerPropertiesFile.of(null));
        }
    }

    @Nested
    class WorldName
    {
        @Test
        void returnsConfiguredValue_when_levelNamePresent() throws IOException
        {
            Path path = writeProperties("""
                    motd=A Minecraft Server
                    level-name=world
                    gamemode=survival
                    """);

            assertEquals("world", ServerPropertiesFile.of(path).worldName());
        }

        @Test
        void returnsDefault_when_levelNameMissing() throws IOException
        {
            Path path = writeProperties("""
                    motd=A Minecraft Server
                    gamemode=survival
                    """);

            assertEquals("world", ServerPropertiesFile.of(path).worldName());
        }

        @Test
        void returnsDefault_when_fileMissing()
        {
            Path missing = tempDir.resolve("missing.properties");

            assertEquals("world", ServerPropertiesFile.of(missing).worldName());
        }
    }

    @Nested
    class WriteProperty
    {
        @Test
        void updateExistingProperty_when_keyPresent() throws IOException
        {
            Path path = writeProperties("""
                    # comment
                    motd=Hello
                    online-mode=true
                    """);

            ServerPropertiesFile.of(path).writeProperty("online-mode", "false");
            List<String> lines = Files.readAllLines(path);

            assertEquals(List.of("# comment", "motd=Hello", "online-mode=false"), lines);
            assertEquals(3, lines.size());
        }

        @Test
        void appendProperty_when_keyAbsent() throws IOException
        {
            Path path = writeProperties("""
                    motd=Hello
                    """);

            ServerPropertiesFile.of(path).writeProperty("online-mode", "false");

            List<String> lines = Files.readAllLines(path);
            assertEquals(List.of("motd=Hello", "online-mode=false"), lines);
        }

        @Test
        void throws_when_keyIsNull() throws IOException
        {
            Path path = writeProperties("motd=x\n");

            assertThrows(NullPointerException.class,
                    () -> ServerPropertiesFile.of(path).writeProperty(null, "value"));
        }

        @Test
        void throws_when_valueIsNull() throws IOException
        {
            Path path = writeProperties("motd=x\n");

            assertThrows(NullPointerException.class,
                    () -> ServerPropertiesFile.of(path).writeProperty("motd", null));
        }

        @Test
        void throws_when_fileMissing()
        {
            Path missing = tempDir.resolve("absent.properties");

            assertThrows(IOException.class,
                    () -> ServerPropertiesFile.of(missing).writeProperty("key", "value"));
        }
    }
}
