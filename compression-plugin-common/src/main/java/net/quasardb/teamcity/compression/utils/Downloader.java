package net.quasardb.teamcity.compression.utils;

import net.quasardb.teamcity.compression.logging.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static net.quasardb.teamcity.compression.utils.FileSystemUtils.backupFile;
import static net.quasardb.teamcity.compression.utils.FileSystemUtils.getLibsDirFile;

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

            File libsDirFile = getLibsDirFile();
            File targetFile = new File(libsDirFile,targetFileName);
            targetFilePath = targetFile.getAbsolutePath();
            if (targetFile.exists()){
                if (!backupOriginalFile){
                    Logger.error("ZSTD Server Plugin: TargetFile: "+ targetFilePath + " exists! Skipping download");
                    return;
                }else {
                    backupFilePath = backupFile(libsDirFile,targetFile);
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
            Files.write(
                    recordsFile.toPath(),
                    deploymentLine.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
