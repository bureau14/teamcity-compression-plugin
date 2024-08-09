package net.quasardb.teamcity.compression.storage;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.artifacts.ArtifactStorageType;
import jetbrains.buildServer.serverSide.artifacts.ArtifactStorageTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import net.quasardb.compression.utils.ZstdConstants;
import net.quasardb.teamcity.compression.settings.ZstdPropertiesProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ZstdStorageType extends ArtifactStorageType {

    private final static Logger LOG = Logger.getInstance(ZstdStorageType.class.getName());
    private final String settingsPath;
    private final ServerSettings serverSettings;


    public ZstdStorageType(@NotNull ArtifactStorageTypeRegistry registry,
                         @NotNull PluginDescriptor descriptor,
                         @NotNull ServerSettings serverSettings) {
        this.settingsPath = descriptor.getPluginResourcesPath(ZstdConstants.ZSTD_SETTINGS_PATH + ".jsp");
        this.serverSettings = serverSettings;
        registry.registerStorageType(this);
    }

    @NotNull
    @Override
    public String getType() {
        return ZstdConstants.ZSTD_STORAGE_TYPE;
    }

    @NotNull
    @Override
    public String getName() {
        return "ZSTD";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Uses zstd archives to store build artifacts";
    }

    @NotNull
    @Override
    public String getEditStorageParametersPath() {
        return settingsPath;
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultParameters() {
        return new HashMap<>();
    }

    @Nullable
    @Override
    public PropertiesProcessor getParametersProcessor() {
        return new ZstdPropertiesProcessor();
    }

    @NotNull
    @Override
    public SettingsPreprocessor getSettingsPreprocessor() {
        return input -> input;
    }
}
