package net.quasardb.teamcity.compression;

import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.util.ArchiveExtractor;
import jetbrains.buildServer.util.ArchiveFileSelector;
import net.quasardb.teamcity.compression.logging.Logger;
import net.quasardb.teamcity.compression.utils.ZstdCompressionUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public interface ZstdExtractor extends ArchiveExtractor {
    String TEMP_BUILD_FOLDER_KEY = "system.teamcity.build.tempDir";
    String TEMP_DEFAULT = "/tmp";
    String TEMP_FILE_PREFIX = "zstd_temp_";
    String TEMP_FILE_SUFFIX = "_compression";
    String ZSTD_COMPRESSION = "zstd";

    ExtensionHolder getExtensionHolder();

    default String getArchiver(@NotNull File archive) throws ArchiveException, IOException {
        try (InputStream is = Files.newInputStream(archive.toPath())) {
            String archiver = ArchiveStreamFactory.detect(is);
            Logger.debug("Detected archiver: " + archiver);
            return archiver;
        }
    }

    @Override
    default boolean isSupported(@NotNull File archive) {
        Logger.info("Call isSupported for "+archive.getName());
        boolean result = false;
        try {
            try(InputStream inputStream = Files.newInputStream(archive.toPath())){
                result = ZstdCompressionUtils.test(inputStream);
            }
        } catch (IOException e) {
            Logger.error("Caught exception during test of archive",e);
        }
        Logger.info("isSupported: "+result);
        return result;
    }

    @Override
    default void extractFiles(@NotNull File archive, @NotNull ArchiveFileSelector archiveFileSelector) throws IOException {
        Logger.info("Call extractFiles " + archive.getName());
        try {
            String buildTempFolder = System.getProperty(TEMP_BUILD_FOLDER_KEY, TEMP_DEFAULT);
            Logger.debug("Temp folder for decompressed file: "+buildTempFolder);

            File targetDirTempFile = new File(buildTempFolder+ FileSystems.getDefault().getSeparator());
            File targetTempFile = File.createTempFile(TEMP_FILE_PREFIX,TEMP_FILE_SUFFIX, targetDirTempFile);

            Path archivePath = archive.toPath();
            Path targetTempFilePath = targetTempFile.toPath();

            Logger.debug("Writing byte array to file: "+targetTempFilePath);

            try (InputStream is = Files.newInputStream(archivePath)) {
                try (CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(ZSTD_COMPRESSION, is)) {
                    Files.copy(in, targetTempFilePath);
                    String archiver = getArchiver(targetTempFile);
                    ArchiveStreamFactory factory = new ArchiveStreamFactory();
                    ArchiveInputStream ais = factory.createArchiveInputStream(archiver,Files.newInputStream(targetTempFilePath));

                }

            } catch (CompressorException e) {
                Logger.error("Compressor exception during handling archive", e);
                throw new IOException(e.getMessage());
            }
        } catch (Exception e){
            Logger.error("Exception during decompression", e);
            throw new IOException(e.getMessage());
        }
    }

    @Override
    default Map<String, Long> getEntitiesSize(@NotNull File archive) throws IOException {
        Logger.info("Call getEntitiesSize for "+archive.getName());
        return ArchiveExtractor.super.getEntitiesSize(archive);
    }

    @Override
    default Map<String, Integer> getEntitiesUnixPermissions(@NotNull File archive) throws IOException {
        Logger.info("Call getEntitiesUnixPermissions for "+archive.getName());
        return ArchiveExtractor.super.getEntitiesUnixPermissions(archive);
    }

    default void register() {
        Logger.info("Registering plugin "+this.getClass().getName()+" with "+getExtensionHolder().toString());
        getExtensionHolder().registerExtension(ArchiveExtractor.class, this.getClass().getName(), this);
    }

}
