package net.quasardb.teamcity.compression.web;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.ExtensionsProvider;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.ProjectManagerEx;
import jetbrains.buildServer.serverSide.artifacts.StoredBuildArtifactInfo;
import jetbrains.buildServer.web.ContentSecurityPolicyConfig;
import jetbrains.buildServer.web.openapi.artifacts.ArtifactDownloadProcessor;
import net.quasardb.compression.utils.ZstdConstants;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ZstdArtifactDownloadProcessor  implements ArtifactDownloadProcessor  {

    private final static Logger LOG = Logger.getInstance(ZstdArtifactDownloadProcessor.class.getName());
    private final ExtensionsProvider extensionsProvider;
    private final ContentSecurityPolicyConfig contentSecurityPolicyConfig;
    private final ProjectManagerEx projectManager;

    public ZstdArtifactDownloadProcessor(@NotNull ExtensionsProvider extensionsProvider,
                                       @NotNull ContentSecurityPolicyConfig contentSecurityPolicyConfig,
                                       @NotNull ProjectManagerEx projectManager) {
        this.extensionsProvider = extensionsProvider;
        this.contentSecurityPolicyConfig = contentSecurityPolicyConfig;
        this.projectManager = projectManager;
    }

    @NotNull
    @Override
    public String getType() {
        return ZstdConstants.ZSTD_STORAGE_TYPE;
    }


    @Override
    public boolean processDownload(@NotNull StoredBuildArtifactInfo storedBuildArtifactInfo, @NotNull BuildPromotion buildPromotion, @NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) throws IOException {
        return false;
    }
}
