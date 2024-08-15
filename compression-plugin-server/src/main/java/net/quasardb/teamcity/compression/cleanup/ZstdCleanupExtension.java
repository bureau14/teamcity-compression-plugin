

package net.quasardb.teamcity.compression.cleanup;


import org.apache.log4j.Logger;
import jetbrains.buildServer.artifacts.ServerArtifactStorageSettingsProvider;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.artifacts.ServerArtifactHelper;
import jetbrains.buildServer.serverSide.cleanup.BuildCleanupContext;
import jetbrains.buildServer.serverSide.cleanup.BuildsCleanupExtension;
import jetbrains.buildServer.serverSide.cleanup.CleanupInterruptedException;
import org.jetbrains.annotations.NotNull;


public class ZstdCleanupExtension implements BuildsCleanupExtension {

    private final static Logger LOG = Logger.getLogger(ZstdCleanupExtension.class.getName());

    public ZstdCleanupExtension(
            @NotNull ServerArtifactHelper helper,
            @NotNull ServerArtifactStorageSettingsProvider settingsProvider,
            @NotNull ServerPaths serverPaths,
            @NotNull ProjectManager projectManager) {
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Zstd artifacts cleaner";
    }

    @Override
    public void prepareBuildsData(@NotNull BuildCleanupContext cleanupContext) {

    }

    @Override
    public void cleanupBuildsData(@NotNull BuildCleanupContext cleanupContext) throws CleanupInterruptedException {

    }

    @Override
    public int getCleanupBuildsDataConcurrencyLevel() {
        return 2;
    }


}
