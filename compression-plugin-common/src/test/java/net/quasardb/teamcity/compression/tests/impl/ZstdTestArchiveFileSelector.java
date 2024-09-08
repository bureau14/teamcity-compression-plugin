package net.quasardb.teamcity.compression.tests.impl;

import jetbrains.buildServer.util.ArchiveFileSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ZstdTestArchiveFileSelector  implements ArchiveFileSelector {
    @Nullable
    @Override
    public File getDestinationFile(@NotNull String archiveEntryName) {
        return null;
    }

    @Nullable
    @Override
    public File getDestinationRoot() {
        return ArchiveFileSelector.super.getDestinationRoot();
    }
}
