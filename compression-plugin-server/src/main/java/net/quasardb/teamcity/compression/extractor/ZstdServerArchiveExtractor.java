package net.quasardb.teamcity.compression.extractor;

import jetbrains.buildServer.ExtensionHolder;
import net.quasardb.teamcity.compression.ZstdExtractor;
import net.quasardb.teamcity.compression.logging.Logger;

public class ZstdServerArchiveExtractor implements ZstdExtractor {
    private final ExtensionHolder extensionHolder;

    public ZstdServerArchiveExtractor(ExtensionHolder extensionHolder) {
        this.extensionHolder = extensionHolder;
        Logger.info("ZSTD Server Extractor loaded");
    }

    @Override
    public ExtensionHolder getExtensionHolder() {
        return this.extensionHolder;
    }

}
