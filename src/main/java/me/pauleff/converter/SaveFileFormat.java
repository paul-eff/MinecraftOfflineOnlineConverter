package me.pauleff.converter;

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
