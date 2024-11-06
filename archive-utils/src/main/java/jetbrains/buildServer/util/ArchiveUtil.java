/*
 * Copyright 2000-2024 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.util.PathUtil;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.*;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.impl.FileAware;
import jetbrains.buildServer.util.impl.SevenZArchiveInputStream;
import net.quasardb.teamcity.compression.ZstdExtractor;
import net.quasardb.teamcity.compression.extractor.ZstdServerArchiveExtractor;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.regex.Pattern.LITERAL;
import static jetbrains.buildServer.util.ArchiveUnixPermissions.FILE_RWXR_XR_X;

/**
 * Provides utils to archive and extract files from zip archive
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public abstract class ArchiveUtil extends BaseArchiveUtil {

  public static final String DEFAULT_ZIP_ENCODING = "cp437";
  /**
   * @since 8.0
   */
  public static final String ARCHIVE_PATH_SEPARATOR = "!/";
  public static final Pattern ARCHIVE_PATH_SEP_PATTERN = Pattern.compile(ARCHIVE_PATH_SEPARATOR, LITERAL);

  private static final Logger LOG = Logger.getInstance(ArchiveUtil.class.getName());
  private static final String GZ = ".gz";
  private static final String TEAMCITY_ARCHIVE_BUFFER_SIZE_PROPERTY = "teamcity.archiveUtil.bufferSize";
  private static final int ARCHIVE_BUFFER_SIZE_BYTES_DEFAULT = 64 * 1024;

  static {
    LOG.info("LOADED PATCHER ARCHIVE UTIL");
  }

  /**
   * Determines the archive type based on a file name.
   * <p>
   * Currently the following extensions are supported:
   * <ul>
   * <li> {@code .zip}, {@code .nupkg}, {@code .snupkg}, {@code .sit}
   * <li> {@code .jar}, {@code .war}, {@code .ear}, {@code .apk}
   * <li> {@code .tar.gz}, {@code .tgz}, {@code .tar.gzip}
   * <li> {@code .tar}
   * </ul>
   *
   * @param name the file or resource name
   * @return archive type
   */
  @NotNull
  public static ArchiveType getArchiveType(@NotNull String name) {
    name = name.toLowerCase();
    if (name.endsWith(".zip") || name.endsWith(".nupkg") || name.endsWith(".snupkg") || name.endsWith(".sit")) {
      return ArchiveType.ZIP;
    } else if (name.endsWith(".jar") || name.endsWith(".war") || name.endsWith(".ear")) {
      return ArchiveType.JAR;
    } else if (name.endsWith(".apk")) {
      return ArchiveType.APK;
    } else if (name.endsWith(".tar.gz") || name.endsWith(".tgz") || name.endsWith(".tar.gzip")) {
      return ArchiveType.TAR_GZ;
    } else if (name.endsWith(".tar")) {
      return ArchiveType.TAR;
//  } else if (name.endsWith(".tar.bz2")) {
//    return ArchiveType.TAR_BZ2;
    } else if (name.endsWith(".7z")) {
      return ArchiveType.SEVEN_Z;
    } else if (name.endsWith(".zst")) {
      return ArchiveType.ZSTD;
    }
    return ArchiveType.NOT_ARCHIVE;
  }

  /**
   * Returns the {@code ArchiveInputStream} for the specified {@code archiveType}.
   * <br />
   * <b>!!!!NOTE. Calling this method for unknown file type may lead to stream leak !!!!</b>
   *
   * @param archiveType the type of archive
   * @param inputStream the input that should be read
   * @return the best possible {@code ArchiveInputStream}, or {@code null} if nothing matches.
   * @throws IOException if I/O error occurs
   */
  @Nullable
  public static ArchiveInputStream getArchiveInputStream(@NotNull ArchiveType archiveType,
                                                         @NotNull InputStream inputStream) throws IOException {
    if (archiveType == ArchiveType.ZSTD) {
      LOG.info("Getting archive input stream from ZSTD");
      try {
        Collection<ZstdExtractor> extractors = ZSTDStaticHolder.getExtensionHolder().getExtensions(ZstdExtractor.class);
        if (extractors.isEmpty()){
          LOG.error("Could not get instance of ZstdExtractor from extension holder");
          return null;
        }
        ZstdExtractor zstdExtractor = extractors.stream().findFirst().get();
        return zstdExtractor.decompressAndGetInputStream(inputStream);
      } catch (Exception e){
        LOG.error("Could not handle ZSTD archive", e);
        return null;
      }
    }

    if (archiveType == ArchiveType.SEVEN_Z) {
      return inputStream instanceof FileAware
        ? new ApacheZipSlipAwareArchiveInputStream(new SevenZArchiveInputStream(((FileAware)inputStream).getFile()))
        : null;
    }

    switch (archiveType) {
      case ZIP:
        return new ApacheZipSlipAwareArchiveInputStream(new ZipArchiveInputStream(inputStream, DEFAULT_ZIP_ENCODING, true));
      case JAR:
        return new ApacheZipSlipAwareArchiveInputStream(new JarArchiveInputStream(inputStream));
      case APK:
        return new ApacheZipSlipAwareArchiveInputStream(new ApacheJavaZipInputStreamBridge(new ZipInputStream(inputStream)));
      case TAR_GZ:
        return new ApacheZipSlipAwareArchiveInputStream(new TarArchiveInputStream(new GZIPInputStream(inputStream), "UTF-8"));
      case TAR:
        return new ApacheZipSlipAwareArchiveInputStream(new TarArchiveInputStream(inputStream, "UTF-8"));
      default:
        return null;
    }
  }

  /**
   * Returns the {@code ArchiveInputStream} for the specified {@code name} of the resource.
   * <br />
   * <b>!!!!NOTE. Calling this method for unknown file type may lead to stream leak !!!!</b>
   *
   * @param name        the name of the resource
   * @param inputStream the input that should be read
   * @return the best possible {@code ArchiveInputStream}, or {@code null} if nothing matches.
   * @throws IOException if I/O error occurs
   */
  @Nullable
  public static ArchiveInputStream getArchiveInputStream(@NotNull String name, @NotNull InputStream inputStream) throws IOException {
    return getArchiveInputStream(getArchiveType(name), inputStream);
  }

  /**
   * Extract files from zip archive
   *
   * @param zip        .zip file to extract
   * @param pathPrefix path prefix to extract from zip. Use "" to extract all files
   * @param targetDir  target folder to extract
   * @return true if operation succeeded, false otherwise
   */
  public static boolean unpackZip(@NotNull final File zip, @NotNull final String pathPrefix, @NotNull final File targetDir) {
    logStartUnpacking(zip, pathPrefix, targetDir);
    try (ZipInputStream file = new ZipSlipAwareZipInputStream(new BufferedInputStream(new FileInputStream(zip)))) {
      unpackZip(file, targetDir, pathPrefix);
    } catch (IOException e) {
      LOG.warnAndDebugDetails("Failed to unpack zip " + zip.getAbsolutePath() + "!" + pathPrefix + " to " + targetDir.getAbsolutePath(), e);
      return false;
    }
    return true;
  }

  public static boolean unpackZip(@NotNull final File zip, @NotNull final File targetDir) {
    return unpackZip(zip, "", targetDir);
  }

  /**
   * Extracts the compressed files from the zip input stream {@code input}
   * and stores them in the {@code targetDir}.
   * <p>
   * The input is closed in the end.
   *
   * @param input     zip input stream
   * @param targetDir the target directory
   * @return true in case of success, false otherwise
   */
  public static boolean unpackZip(@NotNull ZipInputStream input, @NotNull File targetDir) {
    if (!(input instanceof ZipSlipAwareZipInputStream)) {
      LOG.warn("Unsafe usage of ArchiveUtil.unpackZip");
    }
    try {
      unpackZip(input, targetDir, "");
    } catch (IOException e) {
      LOG.warnAndDebugDetails("Failed to unpack zip input stream to " + targetDir.getAbsolutePath(), e);
      return false;
    }
    return true;
  }

  /**
   * Extracts files from zip archive and throws IOException in case there is an error.
   *
   * @param zip            .zip file to extract
   * @param pathPrefix     path prefix to extract from zip. Use "" to extract all files
   * @param targetDir      target folder to extract
   * @param clearTargetDir indicates whether the target dir should be cleared before the unpacking and in case of an error
   */
  public static void unpackZipOrThrow(@NotNull final File zip,
                                      @NotNull final String pathPrefix,
                                      @NotNull final File targetDir,
                                      final boolean clearTargetDir) throws IOException {
    logStartUnpacking(zip, pathPrefix, targetDir);
    if (clearTargetDir) {
      FileUtil.delete(targetDir);
      FileUtil.createEmptyDir(targetDir);
    }
    try (ZipInputStream file = new ZipSlipAwareZipInputStream(new BufferedInputStream(new FileInputStream(zip)))) {
      unpackZip(file, targetDir, pathPrefix);
    } catch (IOException e) {
      if (clearTargetDir) {
        FileUtil.delete(targetDir);
      }
      throw e;
    }
  }

  private static void logStartUnpacking(@NotNull final File zip, @NotNull final String pathPrefix, @NotNull final File targetDir) {
    LOG.debug("Unpacking zip " + zip.getAbsolutePath() + "!" + pathPrefix + " to " + targetDir.getAbsolutePath());
  }

  private static void unpackZip(@NotNull final ZipInputStream file,
                                @NotNull final File targetDir,
                                @NotNull final String pathPrefix) throws IOException {
    ZipEntry ze;
    while ((ze = file.getNextEntry()) != null) {
      if (ze.getName().startsWith(pathPrefix)) {
        saveEntry(targetDir, file, ze, pathPrefix);
      }
    }
  }

  private static void saveEntry(@NotNull final File parentDir,
                                @NotNull final InputStream zf,
                                @NotNull final ZipEntry entry,
                                @NotNull final String pathPrefix)
    throws IOException {

    String relativePath = entry.getName().substring(pathPrefix.length());
    if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
      relativePath = relativePath.substring(1);
    }

    final File file = new File(parentDir, relativePath);

    LOG.debug("Extracting zip entry \"" + entry.getName() + "\" to \"" + file.getAbsolutePath() + "\"");
    FileUtil.createParentDirs(file);
    if (entry.isDirectory()) {
      //noinspection ResultOfMethodCallIgnored
      file.mkdirs();
    } else {
      if (file.exists()) {
        FileUtil.delete(file);
      }

      try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
        TCStreamUtil.writeBinary(new BufferedInputStream(zf), bos);
        bos.flush();
      }
    }
  }

  public static boolean packZip(@NotNull File destFile, @NotNull Collection<File> sourceFiles) throws FileNotFoundException {
    return packZip(destFile, null, sourceFiles);
  }

  /**
   * Packs files and folders to a zip archive. In case of directory all inner files are added recursively.
   *
   * @param destFile    destination zip file
   * @param filter      file name filter
   * @param sourceFiles files or folders to zip
   * @return true in case of success, false otherwise
   * @throws FileNotFoundException if destFile exists but is a directory rather than a regular file,
   *                               does not exist but cannot be created, or cannot be opened for any other reason
   */
  public static boolean packZip(@NotNull File destFile, @Nullable FilenameFilter filter, @NotNull Collection<File> sourceFiles)
    throws FileNotFoundException {
    LOG.debug("Packing " + sourceFiles.size() + " file" + (sourceFiles.size() == 1 ? "" : "s") + " to " + destFile.getAbsolutePath());
    return packZip(new FileOutputStream(destFile), filter, sourceFiles);
  }

  public static boolean packZip(@NotNull OutputStream destStream, @NotNull Collection<File> sourceFiles) {
    return packZip(destStream, null, sourceFiles);
  }

  private static boolean packZip(@NotNull OutputStream destStream, @Nullable FilenameFilter filter, @NotNull Collection<File> sourceFiles) {
    try (ZipOutputStream outputStream = new ZipOutputStream(destStream)) {
      sourceFiles.forEach(file -> packToZip(file, filter, outputStream, false));
      return true;
    } catch (Throwable e) {
      LOG.warn("Failed to pack " + sourceFiles.size() + " file" + (sourceFiles.size() == 1 ? "" : "s"));
      return false;
    }
  }

  /**
   * Packs a file (or a directory) to a zip archive. In case of directory all inner files are added recursively.
   * <p>
   * Files and directories have relative names, for instance, for the following tree:
   * <pre>
   * foo/bar/File1
   *        /File2
   *        /baz/File3
   * </pre>
   * the call {@code packZip(barDirectory, output)} creates a zip with "/File1", "/File2" and "/baz/File3" files,
   * and calls {@code packZip(bazDirectory, output)} and {@code packZip(file3, output)} both create a zip
   * with a single file "/File3".
   * <p>
   * Output stream is closed in the end.
   *
   * @param root   root file or directory
   * @param output the zip stream to write to
   * @return true in case of success, false otherwise
   */
  public static boolean packZip(@NotNull File root, @NotNull ZipOutputStream output) {
    return packZip(root, null, output);
  }

  /**
   * Same as {@link #packZip(File, ZipOutputStream)}, but allows to specify
   * the filename filter additionally.
   *
   * @param root   root file or directory
   * @param filter file name filter
   * @param output the zip stream to write to
   * @return true in case of success, false otherwise
   */
  public static boolean packZip(@NotNull File root, @Nullable FilenameFilter filter, @NotNull ZipOutputStream output) {
    return packToZip(root, filter, output, true);
  }

  /**
   * Assembles a zip archive setting Unix permissions for the archive entries.
   *
   * @param sourceFiles             files to be archived
   * @param resultZip               a path to the result zip
   * @param entitiesUnixPermissions a map from an archive entry path (relative to the archive root) to an integer that represents
   *                                Unix permissions in the format used by {@link ZipArchiveEntry#getUnixMode()}
   * @param markExecutableFiles     if true then files with extensions of executable files will be marked as executable
   * @throws IOException
   */
  public static void packZip(@NotNull final Collection<File> sourceFiles,
                             @NotNull final File resultZip,
                             @NotNull final Map<String, Integer> entitiesUnixPermissions,
                             final boolean markExecutableFiles) throws IOException {
    try (ArchiveOutputStream out = new ZipArchiveOutputStream(new FileOutputStream(resultZip))) {
      for (final File sourceFile : sourceFiles) {
        final byte[] buffer = new byte[TeamCityProperties.getInteger(TEAMCITY_ARCHIVE_BUFFER_SIZE_PROPERTY, ARCHIVE_BUFFER_SIZE_BYTES_DEFAULT)];  // a reusable buffer
        traverseAndWrite(sourceFile, out, new StringBuilder(), true, buffer, entitiesUnixPermissions, markExecutableFiles);
      }
      out.finish();
    } catch (Exception e) {
      LOG.warnAndDebugDetails("Failed to pack zip \"" + resultZip.getAbsolutePath() + "\". " + e.getMessage(), e);
      throw e;
    }
  }

  public static boolean packToZip(@NotNull File root, @Nullable FilenameFilter filter, @NotNull ZipOutputStream output, boolean closeZip) {
    try {
      byte[] buffer = new byte[TeamCityProperties.getInteger(TEAMCITY_ARCHIVE_BUFFER_SIZE_PROPERTY, ARCHIVE_BUFFER_SIZE_BYTES_DEFAULT)];  // a reusable buffer
      try {
        traverseAndWrite(root, filter, output, new StringBuilder(), true, buffer);
      } finally {
        if (closeZip) output.close();
      }
    } catch (IOException e) {
      LOG.warnAndDebugDetails("Failed to pack " + root + " to zip", e);
      return false;
    }
    return true;
  }

  @SuppressWarnings("MethodWithTooManyParameters")
  private static void traverseAndWrite(@NotNull File file,
                                       @Nullable FilenameFilter filter,
                                       @NotNull ZipOutputStream output,
                                       @NotNull StringBuilder pathBuilder,
                                       boolean isFirst,
                                       @NotNull byte[] buffer) throws IOException {
    appendPath(file, pathBuilder, isFirst);

    if (file.isFile()) {
      final String path = pathBuilder.toString();
      final ZipEntry zipEntry = new ZipEntry(path);
      zipEntry.setTime(file.lastModified());
      output.putNextEntry(zipEntry);
      writeFileToZip(file, output, buffer);
    } else {
      final File[] files = filter == null ? file.listFiles() : file.listFiles(filter);
      if (files != null) {
        int length = pathBuilder.length();
        for (File innerFile : files) {
          traverseAndWrite(innerFile, filter, output, pathBuilder, false, buffer);
          pathBuilder.setLength(length);
        }
      }
    }
  }

  private static void writeSymbolicLink(@NotNull File file, @NotNull ArchiveOutputStream output) throws IOException {
    Path symlinkTarget = Files.readSymbolicLink(file.toPath());
    output.write(symlinkTarget.toString().getBytes(StandardCharsets.UTF_8));
  }

  private static void traverseAndWrite(@NotNull final File file,
                                       @NotNull final ArchiveOutputStream output,
                                       @NotNull final StringBuilder pathBuilder,
                                       final boolean isFirst,
                                       @NotNull byte[] buffer,
                                       @NotNull final Map<String, Integer> entitiesUnixPermissions,
                                       final boolean markExecutableFiles) throws IOException {
    appendPath(file, pathBuilder, isFirst);
    if (file.isFile()) {
      final String path = pathBuilder.toString();
      final ZipArchiveEntry zipEntry = new ZipArchiveEntry(path);
      zipEntry.setTime(file.lastModified());
      if (TeamCityProperties.getBooleanOrTrue("teamcity.archiveUtil.settingEntrySize.enabled")) {
        final long fileLength = file.length();
        if (fileLength > 0) {
          zipEntry.setSize(fileLength);
        }
      }
      if (markExecutableFiles && file.getName().toLowerCase().endsWith(".sh")) {
        zipEntry.setUnixMode(FILE_RWXR_XR_X);
      } else if (entitiesUnixPermissions.containsKey(path)) {
        zipEntry.setUnixMode(entitiesUnixPermissions.get(path));
      }
      output.putArchiveEntry(zipEntry);

      if ((zipEntry.getUnixMode() & UnixStat.LINK_FLAG) == UnixStat.LINK_FLAG){
        writeSymbolicLink(file, output);
      } else {
        writeFileToZip(file, output, buffer);
      }
    } else {
      final File[] files = file.listFiles();
      if (files != null) {
        int length = pathBuilder.length();
        for (File innerFile : files) {
          traverseAndWrite(innerFile, output, pathBuilder, false, buffer, entitiesUnixPermissions, markExecutableFiles);
          pathBuilder.setLength(length);
        }
      }
    }
  }

  private static void appendPath(@NotNull final File file, @NotNull final StringBuilder pathBuilder, final boolean isFirst) {
    if (!isFirst || file.isFile()) { // Don't add the name of root directory.
      pathBuilder.append(file.getName());
      if (!file.isFile()) {
        pathBuilder.append('/');  // some problems (in Mac) occurred when using File.separator here
      }
    }
  }

  private static void writeFileToZip(@NotNull final File file, @NotNull final OutputStream output, final byte[] buffer) throws IOException {
    try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
      int read;
      do {
        read = input.read(buffer);
        output.write(buffer, 0, Math.max(read, 0));
      } while (read == buffer.length);
    } catch (Exception e) {
      LOG.warnAndDebugDetails("Failed to pack " + file.getAbsolutePath() + " to zip (error: " + e.getMessage() + ")", e);
      throw e;
    } finally {
      if (output instanceof ArchiveOutputStream) {
        ((ArchiveOutputStream)output).closeArchiveEntry();
      } else if (output instanceof ZipOutputStream) {
        ((ZipOutputStream)output).closeEntry();
      }
    }
  }

  /**
   * Packs the entire content of the input stream and puts it into the output stream.
   * The caller is responsible for closing both streams.
   *
   * @param out output stream
   * @param in  input stream
   */
  public static void packStream(@NotNull final OutputStream out, @NotNull final InputStream in) throws IOException {
    GZIPOutputStream gz = new GZIPOutputStream(out);
    try {
      StreamUtil.copyStreamContent(in, gz);
    } finally {
      gz.finish();
    }
  }

  /**
   * Unpacks the entire content of the input stream and puts it into the output stream.
   * The caller is responsible for closing both streams.
   *
   * @param out output stream
   * @param in  input stream
   */
  public static void unpackStream(@NotNull final OutputStream out, @NotNull final InputStream in) throws IOException {
    StreamUtil.copyStreamContent(new GZIPInputStream(in), out);
  }

  @Nullable
  public static byte[] packBytes(@Nullable byte[] unpacked) {
    try {
      if (unpacked == null) return null;
      ByteArrayOutputStream bas = new ByteArrayOutputStream();
      packStream(bas, new ByteArrayInputStream(unpacked));
      return bas.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to pack bytes", e);
    }
  }

  @Nullable
  public static byte[] unpackBytes(@Nullable byte[] packed) {
    try {
      if (packed == null) return null;
      ByteArrayOutputStream result = new ByteArrayOutputStream(packed.length);
      unpackStream(result, new ByteArrayInputStream(packed));
      return result.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to unpack bytes", e);
    }
  }

  /**
   * Extracts and returns the input stream for the file specified inside a zip stream.
   * If the file cannot be found among zip entries {@code null} is returned.
   * <p>
   * <b>Note</b>: method does not close the {@code input}.
   *
   * @param input the zip input stream
   * @param path  relative path of the file to extract
   * @return the input stream of the file (uncompressed)
   * @throws IOException if I/O error occurs
   */
  @Nullable
  public static InputStream extractEntry(@NotNull ZipInputStream input, @NotNull String path) throws IOException {
    ZipEntry entry;
    while ((entry = input.getNextEntry()) != null) {
      if (entry.getName().equals(path)) {
        return input;
      }
    }
    return null;
  }

  /**
   * Adds ".gz" to the file name
   *
   * @param file file
   * @return new file in the same location
   */
  @NotNull
  public static File getPackedFile(@NotNull final File file) {
    final File parentFile = file.getParentFile();
    final String packedFileName = getPackedFileName(file.getName());
    return parentFile != null ? new File(parentFile, packedFileName) : new File(packedFileName);
  }

  /**
   * adds ".gz" to the given file name
   *
   * @param fileName file name
   * @return fileName + ".gz"
   */
  @NotNull
  public static String getPackedFileName(@NotNull final String fileName) {
    return fileName + GZ;
  }

  public static boolean isPackedFile(@NotNull final File file) {
    return isPackedFileName(file.getName());
  }

  public static boolean isPackedFileName(@NotNull final String fileName) {
    return fileName.endsWith(GZ);
  }

  /**
   * Creates a packed file "file.ext.gz" from the given file "file.ext".
   * If the target file exists before calling this method it will be overwritten.
   *
   * @param srcFile the given file
   * @return the packed file
   * @throws IOException if a problem occurs during file operations
   */
  @NotNull
  public static File packFile(@NotNull final File srcFile) throws IOException {
    final File packedFile = getPackedFile(srcFile);
    packFileTo(packedFile, srcFile);
    return packedFile;
  }

  /**
   * Packs the content of the source file to the destination file.
   * If the destination file exists it will be overwritten.
   *
   * @param dstFile destination file
   * @param srcFile source file
   * @throws IOException if a problem occurs during file operations
   */
  public static void packFileTo(@NotNull final File dstFile, @NotNull final File srcFile) throws IOException {
    try (FileInputStream input = new FileInputStream(srcFile);
         FileOutputStream output = new FileOutputStream(dstFile)) {
      packStream(output, input);
    }
  }

  // copied from com.intellij.util.io.ZipUtil
  public static boolean isZipContainsEntry(File zip, String relativePath) throws IOException {
    try (ZipFile zipFile = new ZipFile(zip)) {
      final Enumeration<? extends ZipEntry> en = zipFile.entries();
      while (en.hasMoreElements()) {
        ZipEntry zipEntry = en.nextElement();
        if (relativePath.equals(zipEntry.getName())) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Returns archived files info for entries in specified archive.
   * Supports inner archives.
   *
   * @param archive           - a particular archive tp process
   * @param innerPathToFolder may contains "!/", e.g. "/", "/folder1/", "/folder/archive.zip!/", "/foo/archive.zip!/baz"
   * @return see above
   * @throws IOException if IOException occurs
   * @since 8.0
   */
  @NotNull
  public static Collection<ArchivedFileInfo> getArchivedFolderEntries(@NotNull final ArchiveFileDescriptor archive, @NotNull final String innerPathToFolder) throws IOException {
    final Deque<String> segments = new LinkedList<>(splitByArchivePathSeparator(innerPathToFolder, true));
    final InputStream archiveInputStream = archive.getInputStream();
    final List<Closeable> toClose = new ArrayList<>();
    toClose.add(archiveInputStream);
    try {
      ArchiveInputStream stream = getArchiveInputStream(archive.getName(), archiveInputStream);
      while (true) {
        if (stream == null) {
          throw new FileNotFoundException("File " + innerPathToFolder + " not found in " + archive + ": archive not supported");
        }
        toClose.add(stream);
        final String segment = StringUtil.removeLeadingSlash(StringUtil.convertAndCollapseSlashes(segments.poll()));
        if (segments.isEmpty()) {
          final ArchiveEntryToArchivedFileInfoConverter converter = new ArchiveEntryToArchivedFileInfoConverter();
          for (ArchiveEntry entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()) {
            final String name = StringUtil.removeLeadingSlash(StringUtil.convertAndCollapseSlashes(entry.getName()));
            if (equalsIgnoringTLSlashes(innerPathToFolder, name)) {
              continue;
            }
            if (!name.startsWith(segment)) {
              continue;
            }
            final String nta = StringUtil.removeLeadingAndTailingSlash(name.substring(segment.length()));
            if (StringUtil.isEmptyOrSpaces(nta)) {
              continue;
            }
            converter.add(nta, entry);
          }
          return converter.convert();
        } else {
          final ArchiveEntry entry = positionArchivedPath(stream, segment);
          if (entry == null) {
            throw new FileNotFoundException("File " + innerPathToFolder + " not found in " + archive);
          }
          final String name = StringUtil.convertAndCollapseSlashes(entry.getName());
          stream = getArchiveInputStream(name, new BufferedInputStream(stream));
        }
      }
    } finally {
      Collections.reverse(toClose);
      for (Closeable closeable : toClose) {
        FileUtil.close(closeable);
      }
    }
  }

  /**
   * Splits path by #ARCHIVE_PATH_SEPARATOR ("!/").
   *
   * @param path          path to process
   * @param withEmptyLast is should include empty string into result if path ends with "!/"
   * @return see above
   * @since 8.0
   */
  @NotNull
  public static List<String> splitByArchivePathSeparator(@NotNull String path, boolean withEmptyLast) {
    List<String> list = new LinkedList<>(Arrays.asList(ARCHIVE_PATH_SEP_PATTERN.split(path)));
    if (withEmptyLast && path.endsWith(ARCHIVE_PATH_SEPARATOR)) {
      list.add("");
    }
    return list;
  }

  @Nullable
  private static ArchiveEntry positionArchivedPath(@NotNull final ArchiveInputStream stream, @NotNull String path) throws IOException {
    for (ArchiveEntry entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()) {
      final String name = StringUtil.convertAndCollapseSlashes(entry.getName());
      if (equalsIgnoringTLSlashes(name, path)) {
        return entry;
      }
    }
    return null;
  }

  private static boolean equalsIgnoringTLSlashes(@NotNull String a, @NotNull String b) {
    a = StringUtil.removeLeadingAndTailingSlash(a);
    b = StringUtil.removeLeadingAndTailingSlash(b);
    return a.equals(b);
  }

  /**
   * Search for entry with specified path in archive and calls callable.
   * Support internal archives
   * Sample paths: "a.txt", "foo/baz.txt", "foo/bar.zip!/baz.txt"
   *
   * @param archive  - a particular archive to search in
   * @param path     - path to search for, e.g. "a.txt", "foo/baz.txt", "foo/bar.zip!/baz.txt"
   * @param callback function called on entry found
   * @param <T>      callback return type
   * @return what callback returns
   * @throws FileNotFoundException if entry not found
   * @throws IOException           if archive processing error or callback returns error or failed to get input stream from archive
   * @since 8.0
   */
  public static <T> T doInArchive(@NotNull final ArchiveFileDescriptor archive, @NotNull final String path, @NotNull final DoInArchiveHandler<T> callback) throws IOException {
    return doInArchive(archive, path, new DoInArchiveHandler2<T>() {
      @Override
      public T found(@NotNull ArchiveEntry entry, @NotNull ArchiveInputStream stream, @NotNull final Deque<Closeable> shouldBeManuallyClosed) throws IOException {
        return callback.found(entry, stream);
      }
    }, true);
  }

  /**
   * Similar to #doInArchive(ArchiveFileDescriptor, String, DoInArchiveHandler)  but callback MUST close Closeable elements (third argument)
   *
   * @since 8.0
   */
  public static <T> T doInArchive(@NotNull final ArchiveFileDescriptor archive, @NotNull final String path, @NotNull final DoInArchiveHandler2<T> callable) throws IOException {
    return doInArchive(archive, path, callable, false);
  }

  private static <T> T doInArchive(@NotNull final ArchiveFileDescriptor archive,
                                   @NotNull final String path,
                                   @NotNull final DoInArchiveHandler2<T> callable,
                                   boolean shouldCloseStreamsOnReturn) throws IOException {
    final Deque<String> segments = new LinkedList<>(splitByArchivePathSeparator(path, false));
    final InputStream artifactStream = archive.getInputStream();

    boolean closeRequired = true;
    final Deque<Closeable> toClose = new LinkedList<>();
    toClose.push(artifactStream);
    try {
      ArchiveInputStream stream = getArchiveInputStream(archive.getName(), artifactStream);
      while (true) {
        if (stream == null) {
          throw new FileNotFoundException("File " + path + " not found in " + archive + ": archive not supported");
        }
        toClose.push(stream);
        final String segment = StringUtil.convertAndCollapseSlashes(segments.poll());
        final ArchiveEntry entry = positionArchivedPath(stream, segment);
        if (entry == null) {
          throw new FileNotFoundException("File " + path + " not found in " + archive);
        }
        final String found = StringUtil.convertAndCollapseSlashes(entry.getName());

        if (segments.isEmpty()) {
          final T result = callable.found(entry, stream, toClose);
          if (!shouldCloseStreamsOnReturn) {
            closeRequired = false;
          }
          return result;
        }
        stream = getArchiveInputStream(found, new BufferedInputStream(stream));
      }
    } finally {
      if (closeRequired) {
        while (!toClose.isEmpty()) {
          FileUtil.close(toClose.pop());
        }
      }
    }
  }

  /**
   * Descriptor for archive file.
   *
   * @since 8.0
   */
  public interface ArchiveFileDescriptor {
    /**
     * Used for construct ArchiveInputStream via #getArchiveInputStream(String, InputStream)
     */
    InputStream getInputStream() throws IOException;

    /**
     * Used for archive type detection.
     *
     * @see ArchiveUtil#getArchiveType(String)
     */
    String getName();

    /**
     * Used for error reporting
     */
    @Override
    String toString();
  }

  /**
   * Callback for #doInArchive(ArchiveFileDescriptor, String, DoInArchiveHandler)
   *
   * @param <T> return type
   * @since 8.0
   */
  public interface DoInArchiveHandler<T> {
    T found(@NotNull final ArchiveEntry entry, @NotNull final ArchiveInputStream stream) throws IOException;
  }

  /**
   * Callback for #doInArchive(ArchiveFileDescriptor, String, DoInArchiveHandler)
   * Similar to DoInArchiveHandler but #found method MUST close all Closeable items in deque(stack)
   *
   * @param <T> return type
   * @since 8.0
   */
  public interface DoInArchiveHandler2<T> {
    T found(@NotNull final ArchiveEntry entry, @NotNull final ArchiveInputStream stream, @NotNull final Deque<Closeable> shouldBeManuallyClosed) throws IOException;
  }

  /**
   * Archived file information descriptor
   *
   * @since 8.0
   */
  public static class ArchivedFileInfo {
    public static final long SIZE_UNKNOWN = -1;
    private final String myName;
    private final long mySize;
    private final long myTimestamp;
    private final boolean myIsDirectory;

    public ArchivedFileInfo(@NotNull String name, long size, long timestamp, boolean isDirectory) {
      myName = name;
      mySize = size;
      myTimestamp = timestamp;
      myIsDirectory = isDirectory;
    }

    public String getName() {
      return myName;
    }

    public long getSize() {
      return mySize;
    }

    public long getTimestamp() {
      return myTimestamp;
    }

    public boolean isDirectory() {
      return myIsDirectory;
    }
  }

  /**
   * Similar to PathUtil#getParentPath(String) but support "!/" path separator and only forward slashes allowed.
   *
   * @param path path to process.
   * @return parent path
   * @since 8.0
   */
  @NotNull
  public static String getParentPath(@NotNull String path) {
    path = StringUtil.convertAndCollapseSlashes(path);
    path = StringUtil.removeTailingSlash(path);
    final String parent = PathUtil.getParentPath(path);
    if (parent.endsWith("!")) { // In case of last separator was "!/", "/" already removed by PathUtil#getParentPath(String)
      return parent.substring(0, parent.length() - 1);
    }
    return parent;
  }

  private static class ArchiveEntryToArchivedFileInfoConverter {
    // value is ArchiveEntry or null for directory
    private final Map<String, ArchiveEntry> myMap = new LinkedHashMap<>();

    public void add(@NotNull String name, @NotNull ArchiveEntry entry) {
      name = StringUtil.removeLeadingAndTailingSlash(name);
      if (name.contains("/")) {
        // generated new directory
        //noinspection ConstantConditions
        myMap.put(name.substring(0, name.indexOf('/')), null);
      } else {
        // 'entry' may be used
        myMap.put(name, entry);
      }
    }

    @NotNull
    public Collection<ArchivedFileInfo> convert() {
      return CollectionsUtil.convertCollection(myMap.entrySet(), source -> {
        final String name = source.getKey();
        long size;
        long timestamp;
        boolean isDirectory;
        final ArchiveEntry entry = source.getValue();
        if (entry != null) {
          size = entry.getSize();
          timestamp = entry.getLastModifiedDate() != null ? entry.getLastModifiedDate().getTime() : ArchivedFileInfo.SIZE_UNKNOWN;
          isDirectory = entry.isDirectory();
        } else {
          size = ArchivedFileInfo.SIZE_UNKNOWN;
          timestamp = 0;
          isDirectory = true;
        }
        return new ArchivedFileInfo(name, size, timestamp, isDirectory);
      });
    }
  }

  public static boolean isNameAllowed(@Nullable final String entryName) {
    // zip specification requires direct slash as file separator
    return entryName == null || !entryName.startsWith("../") && !entryName.contains("/../");
  }
}