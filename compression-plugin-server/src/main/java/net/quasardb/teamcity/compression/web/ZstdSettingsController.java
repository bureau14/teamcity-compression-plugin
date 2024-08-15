

package net.quasardb.teamcity.compression.web;


import org.apache.log4j.Logger;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;

import jetbrains.buildServer.serverSide.*;

import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import net.quasardb.compression.utils.ZstdConstants;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class ZstdSettingsController extends BaseFormXmlController {

  private final static Logger LOG = Logger.getLogger(ZstdSettingsController.class.getName());

  public ZstdSettingsController(@NotNull final WebControllerManager manager,
                                @NotNull final PluginDescriptor descriptor,
                                @NotNull final ServerPaths serverPaths,
                                @NotNull final ProjectManager projectManager) {

    final String path = descriptor.getPluginResourcesPath(ZstdConstants.ZSTD_SETTINGS_PATH + ".html");
    manager.registerController(path, this);
  }

  @Override
  protected ModelAndView doGet(@NotNull final HttpServletRequest request,
                               @NotNull final HttpServletResponse response) {
    return null;
  }

  @Override
  protected void doPost(@NotNull final HttpServletRequest request,
                        @NotNull final HttpServletResponse response,
                        @NotNull final Element xmlResponse) {
    final ActionErrors errors = new ActionErrors();

    if (errors.hasErrors()) {
      errors.serialize(xmlResponse);
    }
  }
}
