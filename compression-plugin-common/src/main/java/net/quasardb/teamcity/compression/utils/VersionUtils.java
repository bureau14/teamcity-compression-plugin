package net.quasardb.teamcity.compression.utils;

import jetbrains.buildServer.version.ServerVersionHolder;

public class VersionUtils {
    public static String getTeamCityVersion(){
        String major = String.valueOf(ServerVersionHolder.getVersion().getDisplayVersionMajor());
        String minor = String.valueOf(ServerVersionHolder.getVersion().getDisplayVersionMinor());
        return major+"."+minor;
    }

    public static String getPluginVersion(){
        return VersionUtils.class.getPackage().getImplementationVersion();
    }
}
