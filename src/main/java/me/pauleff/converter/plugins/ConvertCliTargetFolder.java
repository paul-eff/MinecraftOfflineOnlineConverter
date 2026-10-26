package me.pauleff.converter.plugins;

import me.pauleff.converter.ConverterV3;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Converts every regular file under the CLI {@code -customFolder} path.
 * <p>
 * Enabled only when an online/offline conversion is requested and {@code -customFolder} is set.
 * UUID mappings are prefetched from the server root's usercache. World layout and region
 * detection are skipped so skeleton folders can be converted without a complete world.
 */
public class ConvertCliTargetFolder implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "convert-cli-target-folder",
            "Convert CLI Target Folder",
            "Converts all files in the folder specified by -customFolder.",
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
     * Returns {@code true} when a conversion was requested and {@code -customFolder} is set.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if this plugin should run; {@code false} otherwise
     */
    @Override
    public boolean isEnabled(PluginContext ctx)
    {
        return ctx.isConversionOperation() && ctx.isFolderScopedConversion();
    }

    /**
     * Returns every regular file under the resolved conversion folder.
     *
     * @param ctx the shared conversion context
     * @return all regular files beneath the conversion folder; never {@code null}
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return returnAllFilesInFolders(List.of(ctx.conversionFolder().orElseThrow()));
    }

    /**
     * Runs UUID conversion on the resolved files via {@link ConverterV3}.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets the existing target paths to process
     * @throws IOException if reading or writing files fails
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        logger().info("Converting files in folder: {}", ctx.conversionFolder().orElseThrow());
        new ConverterV3(ctx).convert(resolvedExistingTargets);
    }
}
