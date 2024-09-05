package net.quasardb.teamcity.compression.extractor;

import com.intellij.openapi.util.io.StreamUtil;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.util.ArchiveExtractor;
import jetbrains.buildServer.util.ArchiveFileSelector;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.impl.SevenZArchiveExtractor;
import jetbrains.buildServer.util.impl.TarArchiveExtractor;
import jetbrains.buildServer.util.impl.ZipArchiveExtractor;
import net.quasardb.teamcity.compression.logging.Logger;
import net.quasardb.teamcity.compression.provider.ZstdCompressionProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZstdArchiveExtractor implements ArchiveExtractor {
    private final ZstdCompressionProvider compressionProvider;
    private final static byte[] ZSTD_MAGIC_HEADER = new byte[]{
            (byte)0x28, (byte)0xb5, (byte)0x2f, (byte)0xfd
    };
    private final ExtensionHolder extensionHolder;
    List<ArchiveExtractor> standardExtractors = new ArrayList<>();

    private final static String TEMP_BUILD_FOLDER_KEY = "system.teamcity.build.tempDir";
    private final static String TEMP_DEFAULT = "/tmp";
    private final static String TEMP_FILE_PREFIX = "zstd_temp_";
    private final static String TEMP_FILE_SUFFIX = "_compression";

    public ZstdArchiveExtractor(ZstdCompressionProvider compressionProvider, ExtensionHolder extensionHolder) {
        this.compressionProvider = compressionProvider;
        standardExtractors.add(new TarArchiveExtractor());
        standardExtractors.add(new ZipArchiveExtractor());
        standardExtractors.add(new SevenZArchiveExtractor());
        this.extensionHolder = extensionHolder;
        Logger.info("ZSTD Extractor loaded");
    }

    @Override
    public boolean isSupported( File file) {
        Logger.info("Call isSupported for "+file.getName());
        try {
            byte[] byteArray = Files.readAllBytes(file.toPath());

            return compressionProvider.test(byteArray);

        } catch (IOException e) {
            Logger.error("Caught exception during test of archive",e);
            return false;
        }
    }

    @Override
    public void extractFiles(File file, @NotNull ArchiveFileSelector archiveFileSelector) throws IOException {
        Logger.info("Call extractFiles "+file.getName());
        byte[] byteArray = Files.readAllBytes(file.toPath());
        Logger.debug("Decompressing "+file.getName());
        byte[] decompressed = compressionProvider.decompress(byteArray);

        String buildTempFolder = System.getProperty(TEMP_BUILD_FOLDER_KEY,TEMP_DEFAULT);
        Logger.debug("Temp folder for decompressed file: "+buildTempFolder);

        File targetDirTempFile = new File(buildTempFolder+FileSystems.getDefault().getSeparator());
        File targetTempFile = File.createTempFile(TEMP_FILE_PREFIX,TEMP_FILE_SUFFIX, targetDirTempFile);

        Logger.debug("Writing byte array to file: "+targetTempFile.getPath());
        FileUtils.writeByteArrayToFile(targetTempFile, decompressed);
        //need to figure out is this decompressed file something we support?
        ArchiveExtractor targetExtractor = null;
        for( ArchiveExtractor extractor: standardExtractors){
            if (extractor.isSupported(targetTempFile)){
                //if it is we will delegate everything to other extractors
                targetExtractor = extractor;
                break;
            }
        }

        if(targetExtractor!=null){
            Logger.debug("Target extractor after decompress: "+targetExtractor);
            targetExtractor.extractFiles(targetTempFile,archiveFileSelector);
        } else {
            // no target extractor? is it pure file?
            Logger.debug("Pure file decompressing");
            try {
                String fileName = FilenameUtils.removeExtension(file.getName());
                File destinationFile = archiveFileSelector.getDestinationFile(fileName);
                if (destinationFile != null) {

                        File parentDir = destinationFile.getParentFile();
                        if (parentDir != null) {
                            parentDir.mkdirs();
                        }

                        FileOutputStream writer = new FileOutputStream(destinationFile);
                        FileInputStream reader = new FileInputStream(targetTempFile);

                        try {
                            StreamUtil.copyStreamContent(reader, writer);
                        } finally {
                            FileUtil.close(writer);
                        }
                }
            } catch (Exception e){
                Logger.error("Exception during extraction",e);
            }
        }

    }

    @Override
    public Map<String, Long> getEntitiesSize( File file) throws IOException {
        Logger.info("Call getEntitiesSize for "+file.getName());
        return ArchiveExtractor.super.getEntitiesSize(file);
    }

    @Override
    public Map<String, Integer> getEntitiesUnixPermissions( File file) throws IOException {
        Logger.info("Call getEntitiesUnixPermissions for "+file.getName());
        return ArchiveExtractor.super.getEntitiesUnixPermissions(file);
    }

    public void register() {
        Logger.info("Registering plugin with "+extensionHolder.toString());
        extensionHolder.registerExtension(ArchiveExtractor.class, this.getClass().getName(), this);
    }

}
