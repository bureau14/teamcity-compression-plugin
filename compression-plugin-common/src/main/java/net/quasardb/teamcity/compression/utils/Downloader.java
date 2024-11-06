package net.quasardb.teamcity.compression.utils;

import jetbrains.buildServer.util.ArchiveUtil;
import net.quasardb.teamcity.compression.logging.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

public class Downloader {

    public static void downloadFile(String fileUrl, String targetFileName, boolean backupOriginalFile){
        try{

            URL url = ArchiveUtil.class.getProtectionDomain().getCodeSource().getLocation();
            Logger.info("ZSTD Server Plugin: ArchiveUtil.class location: "+url);
            String serverLibsPath = url.toURI().getPath();
            Logger.info("ZSTD Server Plugin: Local path: "+serverLibsPath);
            File jarFile = new File(serverLibsPath);
            File libsDirFile = jarFile.toPath().getParent().toFile();
            Logger.info("ZSTD Server Plugin: Libs path: "+libsDirFile.getAbsolutePath());
            File targetFile = new File(libsDirFile,targetFileName);
            if (targetFile.exists()){
                if (!backupOriginalFile){
                    Logger.error("ZSTD Server Plugin: TargetFile: "+ targetFile.getAbsolutePath() + " exists! Skipping download");
                    return;
                }else {
                    File backupFile = new File(libsDirFile,targetFileName+"_backup");
                    if(backupFile.exists()){
                        Logger.error("ZSTD Server Plugin: BackupFile: "+ backupFile.getAbsolutePath() + " exists! Will not replace");
                        return;
                    }
                    try{
                        FileUtils.copyFile(targetFile, backupFile);
                    } catch (Exception e){
                        Logger.error("ZSTD Server Plugin: Could not backup file", e);
                    }

                }

            }
            if (libsDirFile.isDirectory()){
                Logger.info("ZSTD Server Plugin: Downloading: "+fileUrl);
                FileUtils.copyURLToFile(
                        new URL(fileUrl),
                        new File(libsDirFile,targetFileName),
                        5000,
                        5000);
                Logger.info("ZSTD Server Plugin: Saved as: "+targetFileName);
            }else{
                Logger.error("ZSTD Server Plugin: Target libs dir -> not a directory");
            }

        } catch (Exception e){
            Logger.error("ZSTD Server Plugin: Could not get location of ArchiveUtil.class", e);
        }
    }
}
