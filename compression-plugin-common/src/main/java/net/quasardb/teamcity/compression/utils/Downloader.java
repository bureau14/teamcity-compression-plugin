package net.quasardb.teamcity.compression.utils;

import jetbrains.buildServer.util.ArchiveUtil;
import net.quasardb.teamcity.compression.logging.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class Downloader {

    private static String DEPLOYMENT_RECORDS_FILE =  "zstd_deployment_records.txt";
    private enum DeploymentStatus {
        SUCCESS,
        FAILURE
    }

    public static synchronized void downloadFile(String fileUrl, String targetFileName, boolean backupOriginalFile){
        String targetFilePath = null;
        String backupFilePath = null;
        DeploymentStatus deploymentStatus = DeploymentStatus.FAILURE;
        try{

            URL url = ArchiveUtil.class.getProtectionDomain().getCodeSource().getLocation();
            Logger.info("ZSTD Server Plugin: ArchiveUtil.class location: "+url);
            String serverLibsPath = url.toURI().getPath();
            Logger.info("ZSTD Server Plugin: Local path: "+serverLibsPath);
            File jarFile = new File(serverLibsPath);
            File libsDirFile = jarFile.toPath().getParent().toFile();
            Logger.info("ZSTD Server Plugin: Libs path: "+libsDirFile.getAbsolutePath());
            File targetFile = new File(libsDirFile,targetFileName);
            targetFilePath = targetFile.getAbsolutePath();
            if (targetFile.exists()){
                if (!backupOriginalFile){
                    Logger.error("ZSTD Server Plugin: TargetFile: "+ targetFilePath + " exists! Skipping download");
                    return;
                }else {
                    File backupFile = new File(libsDirFile,targetFileName+"_backup_"+System.currentTimeMillis());
                    backupFilePath = backupFile.getAbsolutePath();
                    if(backupFile.exists()){
                        Logger.error("ZSTD Server Plugin: BackupFile: "+ backupFilePath + " exists! Will replace");
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
                        targetFile,
                        5000,
                        5000);
                Logger.info("ZSTD Server Plugin: Downloaded file saved as: "+targetFilePath);
                deploymentStatus = DeploymentStatus.SUCCESS;
            }else{
                Logger.error("ZSTD Server Plugin: Target libs dir -> not a directory");
            }

        } catch (Exception e){
            Logger.error("ZSTD Server Plugin: Could not get location of ArchiveUtil.class", e);
        } finally {
            recordDeployment(fileUrl, targetFilePath, backupFilePath, deploymentStatus);
        }
    }

    private static void recordDeployment(String fileUrl, String targetFilePath, String backupFilePath, DeploymentStatus status ){
        File home = new File(System.getProperty("user.home"));
        File recordsFile = new File(home,DEPLOYMENT_RECORDS_FILE);
        String deploymentLine = String.valueOf(System.currentTimeMillis())+"|"+fileUrl+ "|"+String.valueOf(targetFilePath) + "|" + String.valueOf(backupFilePath) + "|" + String.valueOf(status)+ System.lineSeparator();
        try {
            Files.writeString(
                    recordsFile.toPath(),
                    deploymentLine,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
