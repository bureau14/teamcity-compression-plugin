package net.quasardb.teamcity.compression.intercept.agent.impl;

import jetbrains.buildServer.util.ArchiveUtil;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.quasardb.teamcity.compression.intercept.Interceptor;
import net.quasardb.teamcity.compression.intercept.agent.InterceptorAgent;
import net.quasardb.teamcity.compression.intercept.impl.ArchiveUtilInterceptorImpl;
import net.quasardb.teamcity.compression.logging.Logger;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class InterceptorAgentImpl implements InterceptorAgent {

    private final ByteBuddy byteBuddy;

    public InterceptorAgentImpl(){
        this.byteBuddy = new ByteBuddy();
        Logger.info("ZSTD creating interceptor: "+this.byteBuddy);
    }

    public void install(){
        Instrumentation installedInstrumentation = ByteBuddyAgent.install();
        Logger.info("ZSTD instrumentation: "+installedInstrumentation);
        setInterceptor(ArchiveUtil.class,"isArchive", ArchiveUtilInterceptorImpl.class);
    }

    public void setInterceptor(Class target, String targetMethodName, Class interceptor){
        Logger.info("ZSTD set interceptor for "+target+ "."+targetMethodName+" to "+interceptor);
        DynamicType loadedType =this.byteBuddy
                .redefine(target)
                .method(named(targetMethodName))
                .intercept(MethodDelegation.to(interceptor))
                .make()
                .load(target.getClassLoader(),
                        ClassReloadingStrategy.fromInstalledAgent());
        Logger.info("ZSTD interceptor loaded: "+loadedType);
    }
}
