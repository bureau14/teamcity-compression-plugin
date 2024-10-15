package net.quasardb.teamcity.compression.intercept.agent;

import net.quasardb.teamcity.compression.intercept.Interceptor;

public interface InterceptorAgent {
    void install();
    void setInterceptor(Class target, String targetMethodName, Interceptor interceptor);
}
