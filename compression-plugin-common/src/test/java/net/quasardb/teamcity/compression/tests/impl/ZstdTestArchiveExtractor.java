package net.quasardb.teamcity.compression.tests.impl;

import jetbrains.buildServer.ExtensionHolder;
import net.quasardb.teamcity.compression.ZstdExtractor;

public class ZstdTestArchiveExtractor implements ZstdExtractor {
    private final String targetTempFolderDefault;

    public ZstdTestArchiveExtractor(String targetTempFolderDefault) {
        this.targetTempFolderDefault = targetTempFolderDefault;
    }

    @Override
    public String getTempDefault() {
        return this.targetTempFolderDefault;
    }

    @Override
    public ExtensionHolder getExtensionHolder() {
        return null;
    }

}
