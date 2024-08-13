package net.quasardb.compression.provider.zstd;

import com.intellij.openapi.diagnostic.Logger;
import com.github.luben.zstd.Zstd;

public class ZstdCompressionProvider {
    private static final Logger LOG = Logger.getInstance(ZstdCompressionProvider.class.getName());
    private int compressionLevel = 3;

    public ZstdCompressionProvider(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public ZstdCompressionProvider() {}

    public byte[] decompress(byte[] compressedBytes){
        LOG.info("Decompressing with ZSTD");
        final long size = Zstd.getFrameContentSize(compressedBytes);
        byte[] deCompressedBytes = new byte[(int)size];
        Zstd.decompress(deCompressedBytes,compressedBytes);
        return deCompressedBytes;
    }

    public byte[] compress(byte[] bytes){
        LOG.info("Compressing with ZSTD");
        return Zstd.compress(bytes, this.compressionLevel);
    }

    public boolean test(byte[] compressedBytes){
        try{
            long size = Zstd.getFrameContentSize(compressedBytes);
            LOG.debug("Zstd Frame Content size: "+size);
            return size != -1;
        }catch (Exception e){
            LOG.error("Archive is not zstd! {}", e);
        }
        return false;
    }

}
