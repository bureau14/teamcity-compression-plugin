package net.quasardb.teamcity.compression.publish;

import org.apache.log4j.Logger;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.artifacts.AgentArtifactHelper;
import jetbrains.buildServer.agent.artifacts.ArtifactDigestInfo;
import jetbrains.buildServer.artifacts.ArtifactDataInstance;
import jetbrains.buildServer.util.EventDispatcher;
import net.quasardb.compression.provider.zstd.ZstdCompressionProvider;
import net.quasardb.compression.utils.ZstdConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ZstdArtifactsPublisher implements DigestProducingArtifactsPublisher  {

    private final static Logger LOG = Logger.getLogger(ZstdArtifactsPublisher.class.getName());
    private final AgentArtifactHelper agentArtifactHelper;
    private final CurrentBuildTracker currentBuildTracker;
    private final BuildAgentConfiguration buildAgentConfiguration;
    private final ExtensionHolder extensionHolder;
    private volatile ZstdCompressionProvider zstdCompressionProvider;
    private final List<ArtifactDataInstance> artifactsList = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    public ZstdArtifactsPublisher(@NotNull final AgentArtifactHelper helper,
                                @NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher,
                                @NotNull final CurrentBuildTracker tracker,
                                @NotNull final BuildAgentConfiguration buildAgentConfiguration,
                                @NotNull final ZstdCompressionProvider compressionProvider,
                                @NotNull final ExtensionHolder extensionHolder) {
        this.agentArtifactHelper = helper;
        this.currentBuildTracker = tracker;
        this.buildAgentConfiguration = buildAgentConfiguration;
        this.extensionHolder = extensionHolder;
        this.zstdCompressionProvider = compressionProvider;

        dispatcher.addListener(new AgentLifeCycleAdapter() {
            @Override
            public void buildStarted(@NotNull final AgentRunningBuild runningBuild) {
                zstdCompressionProvider = null;
                artifactsList.clear();
            }
        });
    }

    @Override
    public int publishFilesWithDigests(@NotNull Map<File, String> map, @NotNull FlowLogger flowLogger, @Nullable Consumer<ArtifactDigestInfo> consumer) throws ArtifactPublishingFailedException {
        return 0;
    }

    @Override
    public int publishFiles(@NotNull Map<File, String> map) throws ArtifactPublishingFailedException {
        return 0;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @NotNull
    @Override
    public String getType() {
        return ZstdConstants.ZSTD_STORAGE_TYPE;
    }
}
