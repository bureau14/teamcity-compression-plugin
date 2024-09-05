package net.quasardb.teamcity.compression.utils;

import net.quasardb.teamcity.compression.logging.Logger;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.InputStream;

public class ZstdCompressionUtils {

    public static boolean test(InputStream inputStream){
        try{
            String compression = CompressorStreamFactory.detect(inputStream);
            if(compression.equals(CompressorStreamFactory.ZSTANDARD)){
                return true;
            }
        }catch (Exception e){
            Logger.error("Archive is not zstd! {}", e);
        }
        return false;
    }

}
