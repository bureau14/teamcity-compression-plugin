package net.quasardb.teamcity.compression.intercept.impl;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.quasardb.teamcity.compression.intercept.Interceptor;
import net.quasardb.teamcity.compression.logging.Logger;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class ArchiveUtilInterceptorImpl implements Interceptor {
    @RuntimeType
    public static Object intercept(@AllArguments Object[] allArguments, @SuperCall Callable callable) {
        Logger.info("ZSTD Intercepted arguments: "+ Arrays.toString(allArguments) +" callable: "+callable);
        try {
            return callable.call();
        } catch (Exception e){
            Logger.error("Could not intercept method", e);
        }
        return null;
    }
}
