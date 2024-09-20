package net.quasardb.teamcity.compression.tests;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static net.quasardb.teamcity.compression.tests.Utils.getFileFromResources;

public class BaseCompressionTest {

    static File TMP_ROOT = null;

    @BeforeAll
    public static void beforeAll() throws IOException {
        TMP_ROOT = Files.createTempDirectory("zstd_test_run").toFile();
        File resourcesDir = getFileFromResources("zst_test");
        System.out.println("Running tests in "+TMP_ROOT.getAbsoluteFile());
        FileUtils.copyDirectory(resourcesDir, TMP_ROOT);
    }
}
