package net.quasardb.teamcity.compression.utils;

import jetbrains.buildServer.version.ServerVersionHolder;

public class VersionUtils {
    public static String getTeamCityVersion(){
        String major = String.valueOf(ServerVersionHolder.getVersion().getDisplayVersionMajor());
        String minor = String.valueOf(ServerVersionHolder.getVersion().getDisplayVersionMinor());
        if(minor.length()==1){
            minor = "0"+minor;
        }
        return major+"."+minor;
    }

    public static String getPluginVersion(){
        return VersionUtils.class.getPackage().getImplementationVersion();
    }
}
