package me.pauleff.common.handlers.files;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileRenamerTest
{
    @TempDir
    Path tempDir;

    @Nested
    class RenamePreservingExtension
    {
        @Test
        void appendsExtension_when_newNameHasNone() throws IOException
        {
            Path source = Files.writeString(tempDir.resolve("old.dat"), "data");

            Path result = FileRenamer.renamePreservingExtension(source, "new");

            assertEquals(tempDir.resolve("new.dat"), result);
            assertTrue(Files.exists(result));
            assertFalse(Files.exists(source));
            assertEquals("data", Files.readString(result));
        }

        @Test
        void keepsName_when_newNameAlreadyHasExtension() throws IOException
        {
            Path source = Files.writeString(tempDir.resolve("old.dat"), "data");

            Path result = FileRenamer.renamePreservingExtension(source, "new.dat");

            assertEquals(tempDir.resolve("new.dat"), result);
            assertTrue(Files.exists(result));
        }

        @Test
        void notAppend_when_sourceHasNoExtension() throws IOException
        {
            Path source = Files.writeString(tempDir.resolve("README"), "notes");

            Path result = FileRenamer.renamePreservingExtension(source, "LICENSE");

            assertEquals(tempDir.resolve("LICENSE"), result);
            assertTrue(Files.exists(result));
        }

        @Test
        void dontTreatLeadingDotAsExtension() throws IOException
        {
            Path source = Files.writeString(tempDir.resolve(".hidden"), "secret");

            Path result = FileRenamer.renamePreservingExtension(source, "visible");

            assertEquals(tempDir.resolve("visible"), result);
            assertTrue(Files.exists(result));
        }

        @Test
        void replacesExisting_when_targetAlreadyExists() throws IOException
        {
            Path source = Files.writeString(tempDir.resolve("source.txt"), "new-content");
            Files.writeString(tempDir.resolve("target.txt"), "old-content");

            Path result = FileRenamer.renamePreservingExtension(source, "target");

            assertEquals(tempDir.resolve("target.txt"), result);
            assertEquals("new-content", Files.readString(result));
            assertFalse(Files.exists(source));
        }

        @Test
        void throws_when_sourceIsNull()
        {
            assertThrows(NullPointerException.class,
                    () -> FileRenamer.renamePreservingExtension(null, "new"));
        }

        @Test
        void throws_when_newFileNameIsNull() throws IOException
        {
            Path source = Files.writeString(tempDir.resolve("file.txt"), "x");

            assertThrows(NullPointerException.class,
                    () -> FileRenamer.renamePreservingExtension(source, null));
        }
    }
}
