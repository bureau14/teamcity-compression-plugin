package net.quasardb.teamcity.compression.intercept.impl;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.quasardb.teamcity.compression.intercept.Interceptor;
import net.quasardb.teamcity.compression.logging.Logger;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class ArchiveUtilInterceptorImpl implements Interceptor {
    @RuntimeType
    public Object intercept(@Origin Method method,
                            @SuperCall Callable<?> callable) {
        Logger.info("ZSTD Intercepted callable: "+ callable +" method: "+method);
        try {
            return callable.call();
        } catch (Exception e){
            Logger.error("Could not intercept method", e);
        }
        return null;
    }
}
