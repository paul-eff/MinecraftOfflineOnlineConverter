package me.pauleff.converter;

/**
 * Enum representing different Minecraft world formats.
 * Needed, as the worldFormat of specific files, how and where these where saved changed often from first release until today.
 */
public enum WorldFormat
{
    ALPHA("Alpha Minecraft world worldFormat"),
    MC_REGION("Beta Minecraft using the MCR file worldFormat"),
    ANVIL("Minecraft using the MCA file worldFormat"),
    ANVIL_2026("Minecraft using the MCA file and 2026 world worldFormat");

    private final String description;

    /**
     * Constructor for WorldFormat enum.
     *
     * @param description The description of the Minecraft world worldFormat.
     */
    WorldFormat(String description)
    {
        this.description = description;
    }

    /**
     * Returns the description of the Minecraft world worldFormat.
     *
     * @return The description of the Minecraft world worldFormat.
     */
    @Override
    public String toString()
    {
        return description;
    }
}
