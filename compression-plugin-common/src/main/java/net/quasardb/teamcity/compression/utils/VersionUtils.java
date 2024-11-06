package net.quasardb.teamcity.compression.utils;

import jetbrains.buildServer.serverSide.TeamCityProperties;

public class VersionUtils {
    public static String getTeamCityVersion(){
        return TeamCityProperties.getProperty("teamcity.version");
    }

    public static String getPluginVersion(){
        return VersionUtils.class.getPackage().getImplementationVersion();
    }
}
