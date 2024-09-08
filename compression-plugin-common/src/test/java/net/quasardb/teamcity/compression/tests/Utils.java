package net.quasardb.teamcity.compression.tests;

import java.io.File;

public class Utils {
    public static File getFileFromResources(String filePath){
        return new File("src/test/resources/"+filePath);
    }

}
