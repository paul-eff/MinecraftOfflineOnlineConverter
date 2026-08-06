package me.pauleff.common.handlers.files;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TextFileDetectorTest
{
    @TempDir
    Path tempDir;

    @Nested
    class IsTextBased
    {
        @Test
        void returnsTrue_when_fileIsEmpty() throws IOException
        {
            Path empty = Files.createFile(tempDir.resolve("empty.txt"));

            assertTrue(TextFileDetector.isTextBased(empty));
        }

        @Test
        void returnsTrue_when_fileIsPlainAscii() throws IOException
        {
            Path text = Files.writeString(tempDir.resolve("plain.txt"), "Hello, world!\nline two\t");

            assertTrue(TextFileDetector.isTextBased(text));
        }

        @Test
        void returnsFalse_when_fileContainsBytes() throws IOException
        {
            byte[] binary = new byte[256];
            Arrays.fill(binary, (byte) 0);
            Path path = Files.write(tempDir.resolve("binary.bin"), binary);

            assertFalse(TextFileDetector.isTextBased(path));
        }

        @Test
        void returnsFalse_when_nonTextRatioExceedsThreshold() throws IOException
        {
            // Two null bytes in a 100-byte file: count OK (<=2) but ratio 0.02 > 0.01
            byte[] sample = new byte[100];
            Arrays.fill(sample, (byte) 'a');
            sample[0] = 0;
            sample[1] = 0;
            Path path = Files.write(tempDir.resolve("borderline.bin"), sample);

            assertFalse(TextFileDetector.isTextBased(path));
        }

        @Test
        void returnsTrue_when_fewNonTextBytesWithinThreshold() throws IOException
        {
            // Two null bytes in a 200-byte file: count <=2 and ratio == 0.01
            byte[] sample = new byte[200];
            Arrays.fill(sample, (byte) 'a');
            sample[0] = 0;
            sample[1] = 0;
            Path path = Files.write(tempDir.resolve("almost-text.txt"), sample);

            assertTrue(TextFileDetector.isTextBased(path));
        }

        @Test
        void throws_when_pathIsDirectory()
        {
            assertThrows(IllegalArgumentException.class,
                    () -> TextFileDetector.isTextBased(tempDir));
        }

        @Test
        void throws_when_pathDoesNotExist()
        {
            Path missing = tempDir.resolve("missing.txt");

            assertThrows(IllegalArgumentException.class,
                    () -> TextFileDetector.isTextBased(missing));
        }
    }
}
