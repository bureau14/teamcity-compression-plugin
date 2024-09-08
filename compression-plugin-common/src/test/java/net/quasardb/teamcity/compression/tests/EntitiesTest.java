package net.quasardb.teamcity.compression.tests;

import net.quasardb.teamcity.compression.tests.impl.ZstdTestArchiveExtractor;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static net.quasardb.teamcity.compression.tests.Utils.getFileFromResources;

public class EntitiesTest {

    @Test
    public void testEntitiesSizeRawFile() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor(".");
        File zstFile = getFileFromResources("zst_test/test_file.txt.zst");
        Map<String, Long> sizes = extractor.getEntitiesSize(zstFile);
    }

    @Test
    public void testEntitiesSizeTarFile() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor(".");
        File zstFile = getFileFromResources("zst_test/test_folder.tar.zst");
        Map<String, Long> sizes = extractor.getEntitiesSize(zstFile);
    }

    @Test
    public void testEntitiesSizeZipFile() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor(".");
        File zstFile = getFileFromResources("zst_test/test_folder.zip.zst");
        Map<String, Long> sizes = extractor.getEntitiesSize(zstFile);
    }

    @Test
    public void testEntitiesUnixPermissionsRawFile() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor(".");
        File zstFile = getFileFromResources("zst_test/test_folder.txt.zst");
        Map<String, Integer> permissions = extractor.getEntitiesUnixPermissions(zstFile);
    }

    @Test
    public void testEntitiesUnixPermissionsTarFile() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor(".");
        File zstFile = getFileFromResources("zst_test/test_folder.tar.zst");
        Map<String, Integer> permissions = extractor.getEntitiesUnixPermissions(zstFile);
    }

    @Test
    public void testEntitiesUnixPermissionsZipFile() throws IOException {
        ZstdTestArchiveExtractor extractor = new ZstdTestArchiveExtractor(".");
        File zstFile = getFileFromResources("zst_test/test_folder.zip.zst");
        Map<String, Integer> permissions = extractor.getEntitiesUnixPermissions(zstFile);
    }
}
