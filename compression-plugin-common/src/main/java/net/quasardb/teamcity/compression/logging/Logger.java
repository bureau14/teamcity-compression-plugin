package net.quasardb.teamcity.compression.logging;

import jetbrains.buildServer.log.Loggers;

public class Logger {

    public static void info(String message){
        Loggers.SERVER.info(message);
    }

    public static void debug(String message){
        Loggers.SERVER.debug(message);
    }

    public static void error(String message, Throwable e){
        Loggers.SERVER.error(message,e);
    }
}
