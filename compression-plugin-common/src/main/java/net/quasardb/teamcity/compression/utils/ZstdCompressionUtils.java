package net.quasardb.teamcity.compression.utils;

import com.intellij.openapi.util.io.StreamUtil;
import jetbrains.buildServer.util.FileUtil;
import net.quasardb.teamcity.compression.logging.Logger;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class ZstdCompressionUtils {

    public static boolean isZstdCompressed(InputStream inputStream){
        try{
            String compression = CompressorStreamFactory.detect(inputStream);
            if(compression.equals(CompressorStreamFactory.ZSTANDARD)){
                return true;
            }
        }catch (Exception e){
            Logger.error("File is not ZSTD compressed", e);
        }
        return false;
    }

    public static boolean isArchiveOfType(InputStream inputStream, List<String> supportedArchives){
            try {
                String archiver = ArchiveStreamFactory.detect(inputStream);
                Logger.debug("Detected archiver: " + archiver);
                if (supportedArchives.contains(archiver)){
                    return true;
                }
            }catch (Exception e){
                Logger.error("Could not detect archiver type", e);
            }
            return false;
    }

    public static void copyStreamToFile(InputStream input, File target) throws IOException {
        FileOutputStream writer = new FileOutputStream(target);

        try {
            StreamUtil.copyStreamContent(input, writer);
        } finally {
            FileUtil.close(writer);
        }

    }

}
