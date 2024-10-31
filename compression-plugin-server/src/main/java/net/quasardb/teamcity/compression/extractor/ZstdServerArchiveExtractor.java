package net.quasardb.teamcity.compression.extractor;

import com.github.luben.zstd.util.ZstdVersion;
import jdk.internal.loader.BuiltinClassLoader;
import jdk.internal.loader.URLClassPath;
import jetbrains.buildServer.ExtensionHolder;
import net.quasardb.teamcity.compression.ZstdExtractor;
import net.quasardb.teamcity.compression.logging.Logger;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

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
    }

    public static ZstdExtractor get(){
        return INSTANCE;
    }
}
