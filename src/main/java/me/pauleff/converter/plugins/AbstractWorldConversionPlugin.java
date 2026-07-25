package me.pauleff.converter.plugins;

import me.pauleff.converter.ConverterV3;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.ServerTypePlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Base {@link ServerTypePlugin} that converts all files under the world's dimension roots.
 * <p>
 * Subclasses supply server-type compatibility and metadata; targeting and conversion
 * are shared via {@link ConverterV3}.
 */
abstract class AbstractWorldConversionPlugin implements ServerTypePlugin
{
    /**
     * Returns every regular file under the dimension root folders for the detected
     * world folder structure.
     *
     * @param ctx the shared conversion context
     * @return all regular files beneath the dimension roots; never {@code null}
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return returnAllFilesInFolders(
                ctx.worldFolderStructure().dimensionRootFolders(ctx.serverFolder(), ctx.worldFolder()));
    }

    /**
     * Runs UUID conversion on the resolved existing world files via {@link ConverterV3}.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets the existing target paths to process
     * @throws IOException if reading or writing world files fails
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        new ConverterV3(ctx).convert(resolvedExistingTargets);
    }
}
