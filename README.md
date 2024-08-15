# teamcity-compression-plugin

This is project with Compression TeamCity plugin, that adds support for ZSTD and XZ compression for artifacts

1. Build
   Issue 'mvn package' command from the root project to build the plugin. Resulting package <artifactId>.zip will be placed in 'target' directory.

2. Install
   To install the plugin, put zip archive to 'plugins' dir under TeamCity data directory and restart the server.

# To see plugin logs

Add this config to log4j configuration

```xml
     <DelegateAppender>
      <RollingFile name="COMPRESSION.LOG" fileName="${sys:teamcity_logs}/teamcity-compression-plugin.log"
                   filePattern="${sys:teamcity_logs}/teamcity-compression-plugin.log.%i"
                   append="true" createOnDemand="true">
        <PatternLayout pattern="[%d] %6p - %30.30c - %m%n" charset="UTF-8"/>
        <SizeBasedTriggeringPolicy size="10 MB"/>
        <DefaultRolloverStrategy max="3" fileIndex="min"/>
      </RollingFile>
    </DelegateAppender>

<Logger name="net.quasardb" level="DEBUG" additivity="false">
    <AppenderRef ref="COMPRESSION.LOG" />
</Logger>

```