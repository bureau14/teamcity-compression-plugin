package net.quasardb.teamcity.compression.utils;

import net.quasardb.teamcity.compression.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static net.quasardb.teamcity.compression.utils.FileSystemUtils.backupFile;
import static net.quasardb.teamcity.compression.utils.FileSystemUtils.getLibsDirFile;

public class UIUtils {

    public static final String UI_ARCHIVE_EXTENSION_STRING = "zip|nupkg|sit|jar|war|ear|apk|tar\\.gz|tgz|tar\\.gzip|tar|7z";
    public static final String UI_ARCHIVE_EXTENSION_REPLACEMENT_STRING = "zip|zst|nupkg|sit|jar|war|ear|apk|tar\\.gz|tgz|tar\\.gzip|tar|7z";

    public static void modifyBundleJsFile(String relativeDirLocation) {
        File libsDirFile = null;
        try {
            libsDirFile = getLibsDirFile();
        } catch (URISyntaxException e) {
            Logger.error("UIUtils: could not found library/web files location", e);
            return;
        }
        File uiFolderFile = new File(libsDirFile,relativeDirLocation);
        if (uiFolderFile.isDirectory()) {
            File[] matchingJsFiles = uiFolderFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".js") && name.toLowerCase().startsWith("bundle."));
            if (matchingJsFiles != null && matchingJsFiles.length > 0) {
                for (File jsFile : matchingJsFiles) {
                    try {
                        backupFile(uiFolderFile,jsFile);
                        String jsFileContent = new String(Files.readAllBytes(jsFile.toPath()));
                        String updatedContent = jsFileContent.replace(UI_ARCHIVE_EXTENSION_STRING, UI_ARCHIVE_EXTENSION_REPLACEMENT_STRING);
                        Files.write(jsFile.toPath(), updatedContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

                        Logger.info("UIUtils: js string replacement completed successfully!");
                    } catch (IOException e) {
                        Logger.error("UIUtils: could not update js file",e);
                    }

                }
            }else {
                Logger.error("UIUtils: could not find js files in " + uiFolderFile.getAbsolutePath());
            }
        }else{
            Logger.error("UIUtils: provided path is not a directory");
        }
    }
}
