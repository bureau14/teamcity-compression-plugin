package net.quasardb.teamcity.compression.filesystem.impl;

import net.quasardb.teamcity.compression.filesystem.StagingArea;
import net.quasardb.teamcity.compression.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TempStagingArea implements StagingArea {
    private File stagingAreaFile;
    private final String TEMP_FILE_PREFIX = "zstd_temp_";
    private final String TEMP_FILE_SUFFIX = "_decompressed";
    private final List<File> fileCache = new ArrayList<>();

    public TempStagingArea() {}

    @Override
    public synchronized File createTempFile() throws IOException {
        File tempFile =  File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, this.stagingAreaFile);
        Logger.debug("Temp file "+tempFile.getName()+ " created");
        fileCache.add(tempFile);
        return tempFile;
    }

    @Override
    public synchronized boolean cleanup() {
        for(int i=0; i<fileCache.size();i++){
            File tempFile = fileCache.get(i);
            if(tempFile!=null && stagingAreaFile.exists()){
                boolean deleted = tempFile.delete();
                if(deleted){
                    Logger.debug("Temp file "+tempFile.getName()+ " cleaned");
                }else{
                    Logger.error("Could not delete temp file: "+tempFile.getName());
                }
                fileCache.remove(i);
            }else{
                Logger.debug("Temp file is null or does not exists!");
                fileCache.remove(i);
            }
        }
        return fileCache.isEmpty();
    }

    @Override
    public void setParent(String parentFolder) {
        this.stagingAreaFile = new File(parentFolder);
        Logger.debug("Set staging parent: "+stagingAreaFile.getName());
    }
}
