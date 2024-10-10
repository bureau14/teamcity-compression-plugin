package net.quasardb.teamcity.compression.filesystem;

import java.io.File;
import java.io.IOException;

public interface StagingArea {
    File createTempFile() throws IOException;
    boolean cleanup();
    void setParent(String archiveParentFolder);
}
