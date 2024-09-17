package net.quasardb.teamcity.compression.agent.extractor;

import com.github.luben.zstd.util.ZstdVersion;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.AgentExtension;
import net.quasardb.teamcity.compression.ZstdExtractor;
import net.quasardb.teamcity.compression.logging.Logger;

public class ZstdAgentArchiveExtractor implements ZstdExtractor, AgentExtension {

    private final ExtensionHolder extensionHolder;

    public ZstdAgentArchiveExtractor(ExtensionHolder extensionHolder) {
        this.extensionHolder = extensionHolder;
        Logger.info("ZSTD Agent Extractor loaded");
        loadNativeZstdLib();
        Logger.info("ZSTD Agent Lib version: " + ZstdVersion.VERSION);
    }

    @Override
    public ExtensionHolder getExtensionHolder() {
        return this.extensionHolder;
    }
}
