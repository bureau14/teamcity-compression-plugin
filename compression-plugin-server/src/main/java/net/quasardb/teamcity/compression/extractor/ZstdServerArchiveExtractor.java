package net.quasardb.teamcity.compression.extractor;

import com.github.luben.zstd.util.ZstdVersion;
import jetbrains.buildServer.ExtensionHolder;
import net.quasardb.teamcity.compression.ZstdExtractor;
import net.quasardb.teamcity.compression.logging.Logger;

public class ZstdServerArchiveExtractor implements ZstdExtractor {
    private final ExtensionHolder extensionHolder;

    public ZstdServerArchiveExtractor(ExtensionHolder extensionHolder) {
        this.extensionHolder = extensionHolder;
        Logger.info("ZSTD Server Extractor loaded");
        Logger.info("ZSTD Server Native version:" + ZstdVersion.VERSION);
    }

    @Override
    public ExtensionHolder getExtensionHolder() {
        return this.extensionHolder;
    }

}
