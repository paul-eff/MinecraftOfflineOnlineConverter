package me.pauleff.converter;

/**
 * Enum representing different Minecraft world formats.
 * Needed, as the way in which chunk, entity, etc. information was saved on a file level changed over the years.
 */
public enum SaveFileFormat
{
    ALPHA("Alpha Minecraft world format"),
    MC_REGION("Beta Minecraft using the MCR file format"),
    ANVIL("Minecraft using the MCA file format");

    private final String description;

    SaveFileFormat(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
