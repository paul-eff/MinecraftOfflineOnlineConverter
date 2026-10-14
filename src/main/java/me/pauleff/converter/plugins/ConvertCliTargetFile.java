package me.pauleff.converter.plugins;

import me.pauleff.converter.ConverterV3;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Converts the single file specified by the CLI {@code -customFile} option.
 * <p>
 * Enabled only when an online/offline conversion is requested and {@code -customFile} is set.
 * Detection still runs against the full server so save format, world layout, and UUID
 * mappings stay accurate; only the target file is rewritten.
 */
public class ConvertCliTargetFile implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "convert-cli-target-file",
            "Convert CLI Target File",
            "Converts the file specified by -customFile.",
            50);

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    /**
     * Returns {@code true} when a conversion was requested and {@code -customFile} is set.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if this plugin should run; {@code false} otherwise
     */
    @Override
    public boolean isEnabled(PluginContext ctx)
    {
        return ctx.isConversionOperation() && ctx.isFileScopedConversion();
    }

    /**
     * Returns the resolved conversion file as the sole target.
     *
     * @param ctx the shared conversion context
     * @return a single-element list containing the conversion file
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.conversionFile().orElseThrow());
    }

    /**
     * Runs UUID conversion on the resolved file via {@link ConverterV3}.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets the existing target paths to process
     * @throws IOException if reading or writing files fails
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        logger().info("Converting file: {}", ctx.conversionFile().orElseThrow());
        new ConverterV3(ctx).convert(resolvedExistingTargets);
    }
}
