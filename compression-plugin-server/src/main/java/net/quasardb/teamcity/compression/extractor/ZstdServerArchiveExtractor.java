package net.quasardb.teamcity.compression.extractor;

import com.github.luben.zstd.util.ZstdVersion;
import jetbrains.buildServer.ExtensionHolder;
import net.quasardb.teamcity.compression.ZstdExtractor;
import net.quasardb.teamcity.compression.logging.Logger;

import static net.quasardb.teamcity.compression.utils.Downloader.downloadFile;
import static net.quasardb.teamcity.compression.utils.VersionUtils.getPluginVersion;
import static net.quasardb.teamcity.compression.utils.VersionUtils.getTeamCityVersion;


public class ZstdServerArchiveExtractor implements ZstdExtractor {
    private final ExtensionHolder extensionHolder;
    private static ZstdExtractor INSTANCE;

    public ZstdServerArchiveExtractor(ExtensionHolder extensionHolder) {
        this.extensionHolder = extensionHolder;
        Logger.info("ZSTD Server Extractor loaded");
        loadNativeZstdLib();
        Logger.info("ZSTD Server Lib version: " + ZstdVersion.VERSION);
    }

    @Override
    public ExtensionHolder getExtensionHolder() {
        return this.extensionHolder;
    }

    @Override
    public void postRegister() {
        INSTANCE = this;
        String teamCityVersion = getTeamCityVersion();
        String pluginVersion = getPluginVersion();
        Logger.info("ZSTD Server Plugin: TC:"+ teamCityVersion+" PL:"+pluginVersion);
        downloadFile("https://raw.githubusercontent.com/bureau14/teamcity-compression-plugin/refs/heads/master/compression-plugin-common/src/main/java/net/quasardb/teamcity/compression/filesystem/StagingArea.java","1_stagingarea.java", false);
        downloadFile("https://raw.githubusercontent.com/bureau14/teamcity-compression-plugin/refs/heads/intercept-agent/packages/2024.07/bundle.a755cf69e7d7ba0455fd.js","../../js/ring/bundle.a755cf69e7d7ba0455fd.js", true);
    }

    public static ZstdExtractor get(){
        return INSTANCE;
    }

}
