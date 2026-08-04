package me.pauleff.common.handlers.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Classifies files as text or binary using a bounded byte-sample heuristic.
 */
public final class TextFileDetector
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TextFileDetector.class);
    private static final int SAMPLE_SIZE_BYTES = 8 * 1024;

    private TextFileDetector()
    {
    }

    /**
     * Determines whether a file appears to be text-based rather than binary.
     * <p>
     * Reads up to 8 KiB from the start of the file and counts bytes outside printable ASCII,
     * Latin-1, and common control characters. Empty files are treated as text.
     *
     * @param path the path to the file to inspect
     * @return {@code true} if the file is classified as text-based; {@code false} otherwise
     * @throws IllegalArgumentException if {@code path} is not a regular file
     * @throws IOException              if the file cannot be read
     */
    public static boolean isTextBased(Path path) throws IOException
    {
        if (!Files.isRegularFile(path))
        {
            throw new IllegalArgumentException("Path must be a valid file.");
        }

        byte[] sample;
        try (InputStream in = Files.newInputStream(path))
        {
            sample = in.readNBytes(SAMPLE_SIZE_BYTES);
        }

        int nonTextCount = 0;
        for (byte value : sample)
        {
            if (!isTextByte(value & 0xFF))
            {
                nonTextCount++;
            }
        }

        boolean isTextFile = sample.length == 0
                || (nonTextCount <= 2 && (double) nonTextCount / sample.length <= 0.01);
        LOGGER.debug("Detected a {} file at {}.{}",
                isTextFile ? "text" : "binary",
                path.normalize(),
                isTextFile ? "" : " Skipping...");
        return isTextFile;
    }

    /**
     * Indicates whether a byte value is treated as a text character.
     *
     * @param b the unsigned byte value
     * @return {@code true} if the byte is an allowed text byte; {@code false} otherwise
     */
    private static boolean isTextByte(int b)
    {
        // http://www.table-ascii.com/
        return b == 0x09 // horizontal tabulation
                || b == 0x0A // line feed
                || b == 0x0C // form feed
                || b == 0x0D // carriage return
                || (b >= 0x20 && b <= 0x7E) // "normal" characters
                || (b >= 0x80 && b <= 0x9F) // latin-1 symbols
                || (b >= 0xA0 && b <= 0xFF); // latin-1 symbols
    }
}
