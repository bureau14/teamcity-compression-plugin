package net.quasardb.teamcity.compression.tests;

import jetbrains.buildServer.util.ArchiveFileSelector;
import net.quasardb.teamcity.compression.tests.impl.ZstdTestArchiveExtractor;
import net.quasardb.teamcity.compression.tests.impl.ZstdTestArchiveFileSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static net.quasardb.teamcity.compression.tests.Utils.getFileFromResources;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DecompressionTest {


    @Test
    public void testIsSupported(){
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor(".");
        File zstFile = getFileFromResources("zst_test/test_file.txt.zst");
        boolean isSupported = extractor.isSupported(zstFile);
        assertTrue(isSupported, "isSupported() returns incorrect value");
    }

    @Test
    public void testTarDecompression() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor(".");
        File zstFile = getFileFromResources("zst_test/test_folder.tar.zst");
        extractor.extractFiles(zstFile, new ZstdTestArchiveFileSelector());
    }

    @Test
    public void testZipDecompression() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor(".");
        File zstFile = getFileFromResources("zst_test/test_folder.zip.zst");
        extractor.extractFiles(zstFile, new ZstdTestArchiveFileSelector());
    }

    @Test
    public void testRawFileDecompression() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor(".");
        File zstFile = getFileFromResources("zst_test/test_file.txt.zst");
        extractor.extractFiles(zstFile, new ZstdTestArchiveFileSelector());

    }
}
