package me.pauleff.converter;

/**
 * Identifies the on-disk world save format used by a Minecraft server.
 * <p>
 * Each constant carries a human-readable description returned by {@link #toString()}.
 */
public enum SaveFileFormat
{
    /**
     * The Alpha-era Minecraft world format.
     */
    ALPHA("Alpha Minecraft world format"),

    /**
     * The Beta McRegion ({@code .mcr}) world format.
     */
    MC_REGION("Beta Minecraft using the MCR file format"),

    /**
     * The Anvil ({@code .mca}) world format.
     */
    ANVIL("Minecraft using the MCA file format");

    private final String description;

    /**
     * Creates a save format with the given human-readable description.
     *
     * @param description the description shown by {@link #toString()}
     */
    SaveFileFormat(String description)
    {
        this.description = description;
    }

    /**
     * Returns the human-readable description of this save format.
     *
     * @return the description associated with this constant
     */
    @Override
    public String toString()
    {
        return description;
    }
}
