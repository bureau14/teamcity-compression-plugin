# teamcity-compression-plugin

This is project with Compression TeamCity plugin, that adds support for ZSTD and XZ compression for artifacts

## Build
Issue 'mvn package' command from the root project to build the plugin. Resulting package <artifactId>.zip will be placed in 'target' directory.

## Installation
There is several way to install compiled plugin

### Manual install
To install the plugin, put zip archive to 'plugins' dir under TeamCity data directory and restart the server.

### Install through TeamCity UI
1. Go to Administration tab in Team City
2. Open Plugins
3. Upload plugin zip file and click "Enable" button

### Additional libraries installation ( DO NOT REQUIRED WITH VERSION 1.1 )
To decompress zstd archives plugin uses Java JNI binding for zst library. 
This binding could not be loaded with plugin ( despite that plugin already have inside all necessary libs)
To correctly add JNI lib to TeamCity and TeamCity agent please do following:
1. Download library distribution here: https://repo1.maven.org/maven2/com/github/luben/zstd-jni/1.5.6-4/zstd-jni-1.5.6-4.jar
2. Copy downloaded file to <Team-City installation folder>/libs
3. Copy file to <Team-City installation-folder>/buildAgent/libs
4. Restart Team-City server and Agent ( if you run agent as separate os level service)


# Features

This plugin provided basic support for *.zst archives ( on serverside as well as agent side)
1. Now you can use custom tools packaged as *tar.zst, *.zip.zst
2. In the builds dependencies can be specified as \*.zst** => destination

# Logs

Plugin will use root server logger to write logs.

# Patching ArchiveUtil in runtime
To be able to see content of the zst archive in the runtime we need to patch class `ArchiveUtil`
1. Comment ```wrapper.app.parameter.8=-XX:+DisableAttachMechanism``` in the ```buildAgent/launcher/conf/wrapper.conf```
2. In the```buildAgent/launcher/conf/wrapper.conf``` set wrapper java to JDK17 ( as it is max jdk version supported by agent)

If you are not using wrapper and starting agent with custom script 
1. Please set var ```TEAMCITY_JRE=jdk17_location```
2. In the `agent.sh` remove ```-XX:+DisableAttachMechanism``` from jdk options
3. add to the jvm options ```-Djdk.attach.allowAttachSelf=true```

If everything is correct you should see in the log
```
[2024-10-15 22:36:42,268]   INFO -   jetbrains.buildServer.SERVER - ZSTD creating interceptor: net.bytebuddy.ByteBuddy@f372d42a
[2024-10-15 22:36:42,507]   INFO -   jetbrains.buildServer.SERVER - ZSTD instrumentation: sun.instrument.InstrumentationImpl@4545770c
[2024-10-15 22:36:42,521]   INFO -   jetbrains.buildServer.SERVER - ZSTD set interceptor for class jetbrains.buildServer.util.ArchiveUtil.isArchive to class net.quasardb.teamcity.compression.intercept.impl.ArchiveUtilInterceptorImpl
[2024-10-15 22:36:43,135]   INFO -   jetbrains.buildServer.SERVER - ZSTD interceptor loaded: net.bytebuddy.dynamic.DynamicType$Default$Loaded@5667bbc8
```

