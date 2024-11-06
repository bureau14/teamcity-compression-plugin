package net.quasardb.teamcity.compression.utils;

import jetbrains.buildServer.version.ServerVersionHolder;

public class VersionUtils {
    public static String getTeamCityVersion(){
        return ServerVersionHolder.getVersion().getDisplayVersion();
    }

    public static String getPluginVersion(){
        return VersionUtils.class.getPackage().getImplementationVersion();
    }
}
