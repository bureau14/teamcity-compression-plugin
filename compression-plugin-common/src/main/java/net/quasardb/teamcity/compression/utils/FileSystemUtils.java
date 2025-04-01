package net.quasardb.teamcity.compression.utils;

import jetbrains.buildServer.util.ArchiveUtil;
import net.quasardb.teamcity.compression.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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
            Logger.error("ZSTD Server Plugin: BackupFile: "+ backupFilePath + " exists! Checking MD5");
            if (compareFilesMD5(targetFile,backupFile)){
                Logger.info("ZSTD Server Plugin: Backup and target already identical");
                return backupFilePath;
            }
        }
        try{
            FileUtils.copyFile(targetFile, backupFile);
        } catch (Exception e){
            Logger.error("ZSTD Server Plugin: Could not backup file", e);
        }
        return backupFilePath;
    }

    static boolean compareFilesMD5(File source, File destination) {
        try {
            MessageDigest md5=MessageDigest.getInstance("MD5");
            byte[] sourceFileBytes = Files.readAllBytes(source.toPath());
            byte[] sourceFileHash = md5.digest(sourceFileBytes);

            byte[] destFileBytes = Files.readAllBytes(destination.toPath());
            byte[] destFileHash = md5.digest(destFileBytes);
            return Arrays.equals(sourceFileHash, destFileHash);
        } catch (IOException e) {
            Logger.error("ZSTD Server Plugin: Error comparing MD5",e);
        } catch (NoSuchAlgorithmException e) {
            Logger.error("ZSTD Server Plugin: Error getting MD5 algo",e);
        }
        return false;
    }
}
