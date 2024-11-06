package net.quasardb.teamcity.compression.tests.impl;

import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.util.ArchiveExtractor;
import jetbrains.buildServer.util.impl.TarArchiveExtractor;
import jetbrains.buildServer.util.impl.ZipArchiveExtractor;
import net.quasardb.teamcity.compression.ZstdExtractor;
import net.quasardb.teamcity.compression.logging.Logger;
import net.quasardb.teamcity.compression.utils.ZstdCompressionUtils;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

public class ZstdTestArchiveExtractor implements ZstdExtractor {

    public ZstdTestArchiveExtractor() {}

    @Override
    public ExtensionHolder getExtensionHolder() {
        return null;
    }

    @Override
    public ArchiveExtractor getArchiveType(@NotNull File archive) {
        Logger.debug("Call isSupportedArchiveType for " + archive.getName());
        try {
            try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(archive.toPath()))) {
                if(ZstdCompressionUtils.isArchiveOfType(inputStream, Collections.singletonList(ArchiveStreamFactory.TAR))){
                    return new TarArchiveExtractor();
                }
                if(ZstdCompressionUtils.isArchiveOfType(inputStream, Collections.singletonList(ArchiveStreamFactory.ZIP))){
                    return new ZipArchiveExtractor();
                }
            }
        } catch (IOException e) {
            Logger.error("Caught exception during test of archive", e);
        }
        return null;
    }

    @Override
    public void postRegister() {

    }
}
