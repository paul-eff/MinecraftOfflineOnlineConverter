package me.pauleff.common.handlers.files;

import org.json.JSONArray;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UsercacheFileTest
{
    @TempDir
    Path tempDir;

    @Nested
    class Of
    {
        @Test
        void returnsHandle_when_pathProvided()
        {
            Path path = tempDir.resolve("usercache.json");

            UsercacheFile handle = UsercacheFile.of(path);

            assertEquals(path, handle.path());
        }

        @Test
        void throws_when_pathIsNull()
        {
            assertThrows(NullPointerException.class, () -> UsercacheFile.of(null));
        }
    }

    @Nested
    class LoadArray
    {
        @Test
        void parseEntries_when_fileContainsValidJson() throws IOException
        {
            String json = """
                    [
                      {"name":"Steve","uuid":"11111111-1111-1111-1111-111111111111","expiresOn":"2026-08-25 01:04:11 +0200"},
                      {"name":"Alex","uuid":"22222222-2222-2222-2222-222222222222","expiresOn":"2026-08-25 01:04:11 +0200"}
                    ]
                    """;
            Path path = Files.writeString(tempDir.resolve("usercache.json"), json);

            JSONArray result = UsercacheFile.of(path).loadArray();

            assertEquals(2, result.length());
            assertEquals("Steve", result.getJSONObject(0).getString("name"));
            assertEquals("Alex", result.getJSONObject(1).getString("name"));
        }

        @Test
        void returnsEmptyArray_when_fileIsMissing()
        {
            Path missing = tempDir.resolve("missing-usercache.json");

            JSONArray result = UsercacheFile.of(missing).loadArray();

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyArray_when_fileIsEmptyJsonArray() throws IOException
        {
            Path path = Files.writeString(tempDir.resolve("usercache.json"), "[]");

            JSONArray result = UsercacheFile.of(path).loadArray();

            assertTrue(result.isEmpty());
        }
    }
}
