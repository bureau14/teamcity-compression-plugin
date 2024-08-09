package net.quasardb.teamcity.compression.settings;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ZstdPropertiesProcessor implements PropertiesProcessor  {

    private final static Logger LOG = Logger.getInstance(ZstdPropertiesProcessor.class.getName());

    @Override
    public Collection<InvalidProperty> process(Map<String, String> map) {
        return Collections.emptyList();
    }
}