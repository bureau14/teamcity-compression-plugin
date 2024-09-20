package net.quasardb.teamcity.compression.tests.impl;

import jetbrains.buildServer.ExtensionHolder;
import net.quasardb.teamcity.compression.ZstdExtractor;

public class ZstdTestArchiveExtractor implements ZstdExtractor {

    public ZstdTestArchiveExtractor() {}

    @Override
    public ExtensionHolder getExtensionHolder() {
        return null;
    }

}
