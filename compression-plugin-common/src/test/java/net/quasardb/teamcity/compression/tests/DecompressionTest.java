package net.quasardb.teamcity.compression.tests;

import net.quasardb.teamcity.compression.tests.impl.ZstdTestArchiveExtractor;
import net.quasardb.teamcity.compression.tests.impl.ZstdTestArchiveFileSelector;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static net.quasardb.teamcity.compression.tests.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DecompressionTest extends BaseCompressionTest {

    @Test
    public void testIsSupported() {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor();
        File zstFile = new File(TMP_ROOT.getAbsolutePath()+"/test_file.txt.zst");
        boolean isSupported = extractor.isSupported(zstFile);
        assertTrue(isSupported, "isSupported() returns incorrect value");
    }

    @Test
    public void testTarDecompression() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor();
        File zstFile = new File(TMP_ROOT.getAbsolutePath()+"/test_folder.tar.zst");
        extractor.extractFiles(zstFile, new ZstdTestArchiveFileSelector(
                new File("."),
                mapOf(
                        new Utils.KeyValuePair("test_folder/test_file_1.txt", TMP_ROOT.getAbsolutePath()+"/tar_folder1/test_file_1.txt"),
                        new Utils.KeyValuePair("test_folder/test_file_2.txt", TMP_ROOT.getAbsolutePath()+"/tar_folder2/test_file_2.txt")
                )
        ));
        assertTrue(new File(TMP_ROOT.getAbsolutePath()+"/tar_folder1/test_file_1.txt").exists());
        assertTrue(new File(TMP_ROOT.getAbsolutePath()+"/tar_folder2/test_file_2.txt").exists());
    }

    @Test
    public void testZipDecompression() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor();
        File zstFile = new File(TMP_ROOT.getAbsolutePath()+"/test_folder.zip.zst");
        extractor.extractFiles(zstFile, new ZstdTestArchiveFileSelector(
                new File("."),
                mapOf(
                        new Utils.KeyValuePair("test_folder/test_file_1.txt", TMP_ROOT.getAbsolutePath()+"/zip_folder1/test_file_1.txt"),
                        new Utils.KeyValuePair("test_folder/test_file_2.txt", TMP_ROOT.getAbsolutePath()+"/zip_folder2/test_file_2.txt")
                )
        ));

        assertTrue(new File(TMP_ROOT.getAbsolutePath()+"/zip_folder1/test_file_1.txt").exists());
        assertTrue(new File(TMP_ROOT.getAbsolutePath()+"/zip_folder2/test_file_2.txt").exists());
    }

    @Test
    public void testRawFileDecompression() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor();
        File zstFile = new File(TMP_ROOT.getAbsolutePath()+"/test_file.txt.zst");
        extractor.extractFiles(zstFile, new ZstdTestArchiveFileSelector(
                new File("."),
                mapOf(
                        new Utils.KeyValuePair("test_file.txt", TMP_ROOT.getAbsolutePath()+"/expected_new_folder/")
                )));
    }
}
