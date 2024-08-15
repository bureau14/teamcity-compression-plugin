package net.quasardb.teamcity.compression.artifact;

import org.apache.log4j.Logger;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.artifacts.ArtifactContentProvider;
import jetbrains.buildServer.serverSide.artifacts.StoredBuildArtifactInfo;
import net.quasardb.compression.provider.zstd.ZstdCompressionProvider;
import net.quasardb.compression.utils.ZstdConstants;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class ZstdArtifactContentProvider  implements ArtifactContentProvider  {

    private final static Logger LOG = Logger.getLogger(ZstdArtifactContentProvider.class.getName());

    public ZstdArtifactContentProvider(@NotNull ServerPaths serverPaths,
                                        @NotNull ZstdCompressionProvider zstdCompressionProvider) {

    }

    @NotNull
    @Override
    public String getType() {
        return  ZstdConstants.ZSTD_STORAGE_TYPE;
    }

    @NotNull
    @Override
    public InputStream getContent(@NotNull StoredBuildArtifactInfo storedBuildArtifactInfo) throws IOException {
        return null;
    }
}
