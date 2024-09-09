package net.quasardb.teamcity.compression.tests.impl;

import jetbrains.buildServer.util.ArchiveFileSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public class ZstdTestArchiveFileSelector  implements ArchiveFileSelector {

    private final File destinationRoot;
    private final Map<String, String> entryNameToDestinationFile;

    public ZstdTestArchiveFileSelector(File destinationRoot, Map<String,String> entryNameToDestinationFile){
        this.destinationRoot = destinationRoot;
        this.entryNameToDestinationFile = entryNameToDestinationFile;
    }

    @Nullable
    @Override
    public File getDestinationFile(@NotNull String archiveEntryName) {
        String path = entryNameToDestinationFile.get(archiveEntryName);
        if (path!=null) return new File(path);
        return null;
    }

    @Nullable
    @Override
    public File getDestinationRoot() {
        return this.destinationRoot;
    }
}
