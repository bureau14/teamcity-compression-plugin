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

### Additional libraries installation
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

Plugin will ue root serer logger to write logs. 

