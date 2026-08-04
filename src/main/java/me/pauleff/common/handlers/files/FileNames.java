package me.pauleff.common.handlers.files;

/**
 * Provides pure helpers for working with file name strings.
 */
public final class FileNames
{
    private FileNames()
    {
    }

    /**
     * Strips the file extension from a file name.
     * <p>
     * Uses the last {@code '.'} as the extension separator. Names without a qualifying
     * extension are returned unchanged.
     *
     * @param fileName the file name to process
     * @return the file name without its extension, or {@code fileName} if no extension is found
     */
    public static String stripExtension(String fileName)
    {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex).trim() : fileName;
    }
}
