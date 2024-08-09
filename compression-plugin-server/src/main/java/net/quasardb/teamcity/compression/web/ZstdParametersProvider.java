

package net.quasardb.teamcity.compression.web;

import net.quasardb.compression.utils.ZstdConstants;

public class ZstdParametersProvider {
  public String getCompression() {
    return ZstdConstants.ZSTD_COMPRESSION;
  }
}
