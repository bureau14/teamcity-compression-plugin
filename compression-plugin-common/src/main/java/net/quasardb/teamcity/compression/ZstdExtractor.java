package net.quasardb.teamcity.compression;

import com.github.luben.zstd.util.Native;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.util.ArchiveExtractor;
import jetbrains.buildServer.util.ArchiveFileSelector;
import net.quasardb.teamcity.compression.filesystem.StagingArea;
import net.quasardb.teamcity.compression.filesystem.impl.TempStagingArea;
import net.quasardb.teamcity.compression.logging.Logger;
import net.quasardb.teamcity.compression.utils.ZstdCompressionUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public interface ZstdExtractor extends ArchiveExtractor {

    String ZSTD_COMPRESSION = "zstd";

    ExtensionHolder getExtensionHolder();

    default List<ArchiveExtractor> getAvailableArchiveExtractors() {
        return new ArrayList<>(getExtensionHolder().getExtensions(ArchiveExtractor.class));
    }

    default ArchiveExtractor getArchiveType(@NotNull File archive) {
        Logger.debug("Call isSupportedArchiveType for " + archive.getName());

        for( ArchiveExtractor ae: getAvailableArchiveExtractors()){
            if(ae.isSupported(archive)){
                Logger.debug("File "+archive.getName()+" type: " + ae);
                return ae;
            }
        }
        return null;
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
        return result;
    }

    default void processArchive(Path file, ArchiveFileSelector archiveFileSelector, ArchiveExtractor externalArchiveExtractor) throws IOException, ArchiveException {
        if(externalArchiveExtractor!=null){
            externalArchiveExtractor.extractFiles(file.toFile(), archiveFileSelector);
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

    default ArchiveInputStream decompressAndGetInputStream(@NotNull InputStream inputStream) throws CompressorException, IOException, ArchiveException {
        StagingArea stagingArea = new TempStagingArea(true);
        File decompressedTempFile = stagingArea.createTempFile();
        Path decompressedTempFilePath = decompressedTempFile.toPath();
        try (CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(ZSTD_COMPRESSION, inputStream)) {
            Files.copy(in, decompressedTempFilePath, StandardCopyOption.REPLACE_EXISTING);
            try (InputStream is = new BufferedInputStream(Files.newInputStream(decompressedTempFilePath))) {
                return new ArchiveStreamFactory().createArchiveInputStream(is);
            }
        }
    }

    @Override
    default void extractFiles(@NotNull File archive, @NotNull ArchiveFileSelector archiveFileSelector) throws IOException {
        Logger.debug("Call extractFiles " + archive.getName());
        StagingArea stagingArea = new TempStagingArea();

        try {
            String archiveParentFolder = archive.getParent();
            Logger.debug("Folder for decompressed file: " + archiveParentFolder);
            stagingArea.setParent(archiveParentFolder);
            File decompressedTempFile = stagingArea.createTempFile();

            Path archivePath = archive.toPath();
            Path decompressedTempFilePath = decompressedTempFile.toPath();

            Logger.debug("Writing byte array to file: " + decompressedTempFilePath);

            try (InputStream is = Files.newInputStream(archivePath)) {
                try (CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(ZSTD_COMPRESSION, is)) {
                    Files.copy(in, decompressedTempFilePath, StandardCopyOption.REPLACE_EXISTING);

                    ArchiveExtractor supportedExtractor = getArchiveType(decompressedTempFile);
                    if (supportedExtractor!=null) {
                        processArchive(decompressedTempFilePath, archiveFileSelector, supportedExtractor);
                    } else {
                        processSingleFile(decompressedTempFilePath, archiveFileSelector);
                    }

                }
            }
        } catch (Exception e) {
            Logger.error("ZSTD Exception during decompression", e);
            throw new IOException(e.getMessage());
        } finally {
            Logger.debug("Executing cleanup for temp files");
            boolean cleaned = stagingArea.cleanup();
            Logger.debug("Cleanup status: "+ cleaned);
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
        postRegister();
    }

    void postRegister();

    default void loadNativeZstdLib(){
        Logger.debug("ZSTD Loading Native lib...");
        Native.load();
        Logger.info("ZSTD Native Library loaded: "+Native.isLoaded());
        Logger.debug("ZSTD Plugin ClassLoader: "+this.getClass().getClassLoader().toString());
    }

}
