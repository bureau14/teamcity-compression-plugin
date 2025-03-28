package net.quasardb.teamcity.compression.utils;

import jetbrains.buildServer.util.ArchiveUtil;
import net.quasardb.teamcity.compression.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class FileSystemUtils {
    @NotNull
    static File getLibsDirFile() throws URISyntaxException {
        URL url = ArchiveUtil.class.getProtectionDomain().getCodeSource().getLocation();
        Logger.info("ZSTD Server Plugin: ArchiveUtil.class location: "+url);
        String serverLibsPath = url.toURI().getPath();
        Logger.info("ZSTD Server Plugin: Local path: "+serverLibsPath);
        File jarFile = new File(serverLibsPath);
        File libsDirFile = jarFile.toPath().getParent().toFile();
        Logger.info("ZSTD Server Plugin: Libs path: "+libsDirFile.getAbsolutePath());
        return libsDirFile;
    }

    static String backupFile(@NotNull final File parentFolder, final File targetFile) {
        File backupFile = new File(parentFolder,targetFile.getName()+"_backup_"+System.currentTimeMillis());
        String backupFilePath = backupFile.getAbsolutePath();
        if(backupFile.exists()){
            Logger.error("ZSTD Server Plugin: BackupFile: "+ backupFilePath + " exists! Will replace");
            return null;
        }
        try{
            FileUtils.copyFile(targetFile, backupFile);
        } catch (Exception e){
            Logger.error("ZSTD Server Plugin: Could not backup file", e);
        }
        return backupFilePath;
    }
}
