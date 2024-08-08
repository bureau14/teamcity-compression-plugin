# teamcity-compression-plugin

This is project with Compression TeamCity plugin, that adds support for ZSTD and XZ compression for artifacts

1. Build
   Issue 'mvn package' command from the root project to build the plugin. Resulting package <artifactId>.zip will be placed in 'target' directory.

2. Install
   To install the plugin, put zip archive to 'plugins' dir under TeamCity data directory and restart the server.
