package net.quasardb.teamcity.compression;

import com.github.luben.zstd.util.Native;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.util.ArchiveExtractor;
import jetbrains.buildServer.util.ArchiveFileSelector;
import net.quasardb.teamcity.compression.logging.Logger;
import net.quasardb.teamcity.compression.utils.ZstdCompressionUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Map;

import static net.quasardb.teamcity.compression.utils.ZstdCompressionUtils.copyStreamToFile;

public interface ZstdExtractor extends ArchiveExtractor {
    String TEMP_FILE_PREFIX = "zstd_temp_";
    String TEMP_FILE_SUFFIX = "_decompressed";
    String ZSTD_COMPRESSION = "zstd";

    ExtensionHolder getExtensionHolder();

    default boolean isSupportedArchiveType(@NotNull File archive) {
        Logger.debug("Call isSupportedArchiveType for " + archive.getName());
        boolean result = false;
        try {
            try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(archive.toPath()))) {
                result = ZstdCompressionUtils.isArchiveOfType(inputStream, Arrays.asList(ArchiveStreamFactory.TAR, ArchiveStreamFactory.ZIP));
            }
        } catch (IOException e) {
            Logger.error("Caught exception during test of archive", e);
        }
        Logger.debug("isSupportedArchiveType: " + result);
        return result;
    }

    @Override
    default boolean isSupported(@NotNull File archive) {
        Logger.debug("Call isSupported for " + archive.getName());
        boolean result = false;
        try {
            try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(archive.toPath()))) {
                result = ZstdCompressionUtils.isZstdCompressed(inputStream);
            }
        } catch (IOException e) {
            Logger.error("Caught exception during compression test of archive", e);
        }
        Logger.info("isSupported: " + result);
        return result;
    }

    default void processArchive(Path file, ArchiveFileSelector archiveFileSelector) throws IOException, ArchiveException {
        File root = archiveFileSelector.getDestinationRoot();
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(file));
        ArchiveInputStream archiveStream = factory.createArchiveInputStream(bufferedInputStream);
        ArchiveEntry entry;
        while ((entry = archiveStream.getNextEntry()) != null) {
            File destinationFile = archiveFileSelector.getDestinationFile(entry.getName());
            if (destinationFile != null) {
                if (entry.isDirectory()) {
                    destinationFile.mkdirs();
                    this.postProcessEntry(entry, destinationFile, root);
                } else {
                    File parentDir = destinationFile.getParentFile();
                    if (parentDir != null) {
                        parentDir.mkdirs();
                    }
                    copyStreamToFile(archiveStream, destinationFile);
                    this.postProcessEntry(entry, destinationFile, root);
                }
            }
        }
    }

    default void processSingleFile(Path file, ArchiveFileSelector archiveFileSelector) throws IOException {
        File destinationFile = archiveFileSelector.getDestinationFile(file.toFile().getName());
        if (destinationFile != null) {
            File parentDir = destinationFile.getParentFile();
            if (parentDir != null) {
                parentDir.mkdirs();
            }
            Files.copy(file, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    default void postProcessEntry(@NotNull ArchiveEntry entry, @NotNull File file, @Nullable File root) {
        file.setLastModified(entry.getLastModifiedDate().getTime());
    }

    @Override
    default void extractFiles(@NotNull File archive, @NotNull ArchiveFileSelector archiveFileSelector) throws IOException {
        Logger.debug("Call extractFiles " + archive.getName());
        try {
            String archiveParentFolder = archive.getParent();
            Logger.debug("Folder for decompressed file: " + archiveParentFolder);

            File targetDirTempFile = new File(archiveParentFolder);
            File decompressedTempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, targetDirTempFile);

            Path archivePath = archive.toPath();
            Path decompressedTempFilePath = decompressedTempFile.toPath();

            Logger.debug("Writing byte array to file: " + decompressedTempFilePath);

            try (InputStream is = Files.newInputStream(archivePath)) {
                try (CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(ZSTD_COMPRESSION, is)) {
                    Files.copy(in, decompressedTempFilePath, StandardCopyOption.REPLACE_EXISTING);

                    boolean isArchive = isSupportedArchiveType(decompressedTempFile);
                    if (isArchive) {
                        processArchive(decompressedTempFilePath, archiveFileSelector);
                    } else {
                        processSingleFile(decompressedTempFilePath, archiveFileSelector);
                    }

                }
            }
        } catch (Exception e) {
            Logger.error("ZSTD Exception during decompression", e);
            throw new IOException(e.getMessage());
        }
    }

    @Override
    default Map<String, Long> getEntitiesSize(@NotNull File archive) throws IOException {
        Logger.debug("Call getEntitiesSize for " + archive.getName());
        return ArchiveExtractor.super.getEntitiesSize(archive);
    }

    @Override
    default Map<String, Integer> getEntitiesUnixPermissions(@NotNull File archive) throws IOException {
        Logger.debug("Call getEntitiesUnixPermissions for " + archive.getName());
        return ArchiveExtractor.super.getEntitiesUnixPermissions(archive);
    }

    default void register() {
        Logger.debug("Registering plugin " + this.getClass().getName() + " with " + getExtensionHolder().toString());
        getExtensionHolder().registerExtension(ArchiveExtractor.class, this.getClass().getName(), this);
    }

    default void loadNativeZstdLib(){
        Logger.debug("ZSTD Loading Native lib...");
        Native.load();
        Logger.info("ZSTD Native Library loaded: "+Native.isLoaded());
        Logger.debug("ZSTD Plugin ClassLoader: "+this.getClass().getClassLoader().toString());
    }

}
