package us.kolmafia.multitool;

import static us.kolmafia.multitool.Constants.KOLMAFIA_NAME;
import static us.kolmafia.multitool.Constants.MULTITOOL_NAME;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class Multitool {
  static String cwd;
  private static String localJava;
  private static int localJavaVersion;
  static PrintWriter logWriter;
  static String logFileName;

  public static void main(String[] args) {
    initLogOrExit();
    localJavaVersion = getLocalJavaVersion();
    String separator = FileSystems.getDefault().getSeparator();
    localJava = cleanPath(System.getProperty("java.home") + separator + "bin" + separator + "java");
    cwd = cleanPath(Paths.get("").toAbsolutePath().toString());
    int localToolVersion = getLocalVersion(MULTITOOL_NAME);
    int remoteToolVersion = getLatestReleaseVersion(MULTITOOL_NAME);
    if (localToolVersion < remoteToolVersion) {
      String toolName = MULTITOOL_NAME;
      int version = remoteToolVersion;
      String remoteFile =
          "https://github.com/kolmafia/"
              + toolName
              + "/releases/download/r"
              + version
              + "/"
              + toolName
              + "-"
              + version
              + ".jar";
      downloadAFile(remoteFile);
      startNewJVMAndExit(toolName, version);
    }
    removeExtraVersions(MULTITOOL_NAME, localToolVersion);
  }

  private static void removeExtraVersions(String toolName, int toolVersion) {
    File f = new File(cwd);
    String[] files = f.list();
    if (files != null) {
      for (String file : files) {
        VersionData verDat = getVersionDataFromFilename(file, toolName);
        int candidate = verDat.getVersion();
        if ((candidate > 0) && (candidate != toolVersion)) {
          File deleteLater = new File(file);
          deleteLater.deleteOnExit();
        }
      }
    }
  }

  private static void startNewJVMAndExit(String toolName, int version) {
    String path = localJava;
    String jar = toolName + "-" + version + ".jar";
    String command = path + ".exe";
    command = "java";
    // This works because Java is in Path.  Need alternative or find out what is running current
    // process
    String args = "-jar " + jar;
    logWriter.println("Starting " + command + " " + args);
    cleanUpLog();
    ProcessBuilder pb = new ProcessBuilder(command, args);
    pb.directory(null);
    try {
      pb.start();
    } catch (IOException e) {
      System.out.println("Problem stating " + jar + ": " + e.getMessage());
    }
    System.exit(0);
  }

  public static void cleanUpLog() {
    logWriter.println("Log closed at " + formattedTimeNow());
    logWriter.flush();
    logWriter.close();
  }

  /**
   * Looks for files that satisfy the naming convention in the current working directory. Extracts
   * the version numbers and returns the highest one unless there are no files satisfying the naming
   * convention in which case zero is returned.
   *
   * @param toolName Tool name used a prefix
   * @return Highest version of tool or zero
   */
  private static int getLocalVersion(String toolName) {
    int retVal = 0;
    try {
      File f = new File(cwd);
      String[] files = f.list();
      if (files != null) {
        for (String file : files) {
          VersionData verDat = getVersionDataFromFilename(file, toolName);
          int candidate = verDat.getVersion();
          if (candidate > retVal) {
            retVal = candidate;
          }
        }
      }
    } catch (Exception e) {
      String message = "Problem creating " + cwd + " because " + e.getMessage();
      System.out.println(message);
      logWriter.println(message);
    }
    return retVal;
  }

  public static void Oldmain(String[] args) {
    initLogOrExit();
    processLocalInformation();
    int preferredJava = getPreferredJava();
    ToolData multiData = processTool(MULTITOOL_NAME);
    ToolData mafiaData = processTool(KOLMAFIA_NAME);
    if (multiData.isNeedToDownload()) {
      downloadAFile(multiData.getDownloadURL());
      logWriter.println("***");
      logWriter.println("Downloaded newer version of " + MULTITOOL_NAME + ".");
      logWriter.println("***");
      multiData = processTool(MULTITOOL_NAME);
    }
    if (mafiaData.isNeedToDownload()) {
      downloadAFile(mafiaData.getDownloadURL());
      logWriter.println("***");
      logWriter.println("Downloaded newer version of " + KOLMAFIA_NAME + "'");
      logWriter.println("***");
      mafiaData = processTool(KOLMAFIA_NAME);
    }
    displayLocalInformation();
    logWriter.println("Preferred Java version: " + preferredJava);
    if (preferredJava > localJavaVersion) {
      logWriter.println("Local Java too low for " + KOLMAFIA_NAME + ".  Running disabled.");
    }
    displayToolInformation(multiData);
    displayToolInformation(mafiaData);
    if (args.length > 0) {
      if (args[0].equalsIgnoreCase("run")) {
        try {
          if (preferredJava <= localJavaVersion) {
            startSecondJVM(mafiaData);
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    logWriter.close();
    System.exit(0);
  }

  static void processLocalInformation() {
    String separator = FileSystems.getDefault().getSeparator();
    cwd = cleanPath(Paths.get("").toAbsolutePath().toString());
    localJava = cleanPath(System.getProperty("java.home") + separator + "bin" + separator + "java");
    localJavaVersion = getLocalJavaVersion();
  }

  public static void displayLocalInformation() {
    logWriter.println("Current working directory: " + cwd);
    logWriter.println("Path to local Java: " + localJava);
    logWriter.println("Local Java version: " + localJavaVersion);
  }

  private static int getPreferredJava() {
    String rel = "https://raw.githubusercontent.com/kolmafia/kolmafia/refs/heads/main/README.md";
    String retVal;
    StringBuilder buffer = new StringBuilder();
    URL url;
    try {
      url = new URL(rel);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    try (InputStream is = url.openStream()) {
      int ptr;
      while (true) {
        try {
          if ((ptr = is.read()) == -1) break;
        } catch (IOException e) {
          System.out.println("Unexpected error reading from " + url + ": " + e.getMessage());
          return 0;
        }
        buffer.append((char) ptr);
      }
    } catch (IOException e) {
      System.out.println("Problem opening " + url + ": " + e.getMessage());
      return 0;
    }
    String js = buffer.toString();
    String findMe = "java&message=v";
    int i = js.indexOf(findMe);
    js = js.substring(i + findMe.length());
    i = js.indexOf("&");
    js = js.substring(0, i);
    retVal = js;
    if (isAllDigits(retVal)) {
      return Integer.parseInt(retVal);
    } else {
      return 0;
    }
  }

  private static ToolData processTool(String toolName) {
    ToolData retVal = new ToolData(toolName);
    retVal.setLatestVersion(getLatestReleaseVersion(toolName));
    int version = retVal.getLatestVersion();
    String remoteFile =
        "https://github.com/kolmafia/"
            + toolName
            + "/releases/download/r"
            + version
            + "/"
            + toolName
            + "-"
            + version
            + ".jar";
    retVal.setDownloadURL(remoteFile);
    retVal.setLocalJars(processDirectory(toolName));
    retVal.setLocalModificationFound(false);
    List<String> locals = retVal.getLocalJars();
    String runMe = "";
    int localVersion = 0;
    for (String systemJarName : locals) {
      String jarName = systemJarName.toLowerCase();
      VersionData verDat = getVersionDataFromFilename(jarName, toolName);
      runMe = systemJarName;
      localVersion = verDat.getVersion();
      retVal.setLocalModificationFound(verDat.isModified());
    }
    retVal.setCurrentVersion(localVersion);
    retVal.setNeedToDownload(localVersion < version);
    retVal.setLatestJarFile(Paths.get(runMe).toFile());
    return retVal;
  }

  private static void displayToolInformation(ToolData tool) {
    logWriter.println(tool);
  }

  private static void downloadAFile(String location) {
    String localName = location.substring(location.lastIndexOf("/") + 1);
    InputStream in;
    try {
      in = new URL(location).openStream();
      Files.copy(in, Paths.get(localName), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      System.out.println(
          "Failed to open " + location + " or copy to " + localName + " because " + e.getMessage());
    }
  }

  public static void startSecondJVM(ToolData tool) throws Exception {
    String path = localJava;
    String jar = tool.getLatestJarFile().getCanonicalPath();
    String command = path + " -jar " + jar;
    logWriter.println(command);
    Runtime.getRuntime().exec(command);
  }

  /**
   * For Java 9 and beyond the Java version number will begin with X.Y.Z with X, Y and Z being
   * positive integers representing Major release number, Minor release number and Patch release
   * number. Since this code is not intended to work on anything less that Java 11 only that format
   * will be expected and only the major version number reported. Note that an alternative
   * implementation for Java 9 and beyond is to use Runtime.version() instead of the system
   * property.
   *
   * @return Major java version of current JRE or zero
   */
  static int getLocalJavaVersion() {
    String locStr = System.getProperty("java.version");
    int i = locStr.indexOf(".");
    String num = locStr.substring(0, i);
    if (isAllDigits(num)) {
      return Integer.parseInt(num);
    } else {
      return 0;
    }
  }

  private static int getLatestReleaseVersion(String tool) {
    String rel = "https://api.github.com/repos/kolmafia/" + tool + "/releases/latest";
    URL url;
    try {
      url = new URL(rel);
    } catch (MalformedURLException e) {
      System.out.println("Problem accessing releases repo for " + tool + ": " + e.getMessage());
      return 0;
    }
    try (InputStream is = url.openStream()) {
      int version = getVersionFromInputStream(is);
      is.close();
      return version;
    } catch (IOException e) {
      System.out.println("Problem opening or closing " + url + ": " + e.getMessage());
      return 0;
    }
  }

  /**
   * Uses an opened input stream to determine the latest version of a tool in a remote repository.
   * Caller needs to close the input stream.
   *
   * @param is Successfully opened input stream to remote repository.
   * @return latest version in repository or zero
   */
  static int getVersionFromInputStream(InputStream is) {
    {
      StringBuilder buffer = new StringBuilder();
      int ptr;
      while (true) {
        try {
          if ((ptr = is.read()) == -1) break;
        } catch (IOException e) {
          System.out.println(
              "Unexpected error reading from remote input stream: " + e.getMessage());
          return 0;
        }
        buffer.append((char) ptr);
      }
      JsonReader reader = Json.createReader(new StringReader(buffer.toString()));
      JsonObject jsonObject = reader.readObject();
      String name = jsonObject.getString("name");
      reader.close();
      if (isAllDigits(name)) {
        return Integer.parseInt(name);
      } else {
        return 0;
      }
    }
  }

  static List<String> processDirectory(String nameRoot) {
    // Returns a list of file names in the current directory that match
    List<String> retVal = new ArrayList<>();
    String lcRoot = nameRoot.toLowerCase();
    try {
      File f = new File(cwd);
      String[] files = f.list();
      if (files != null) {
        for (String file : files) {
          String check = file.toLowerCase();
          if ((check.startsWith(lcRoot)) && (check.endsWith(".jar"))) {
            String ver = check.substring(lcRoot.length() + 1, check.length() - 4);
            if (ver.endsWith("-m")) {
              ver = ver.substring(0, ver.length() - 2);
            }
            boolean isNumeric = ver.chars().allMatch(Character::isDigit);
            if (isNumeric) {
              retVal.add(file);
            } else {
              logWriter.println(
                  "Local file " + file + " does not match expected naming condition.  Skipping.");
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Problem creating " + cwd + " because " + e.getMessage());
    }
    return retVal;
  }

  static String cleanPath(String path) {
    String retVal = path;
    String fs = "/";
    String bs = "\\\\";
    retVal = retVal.replaceAll(bs, fs);
    retVal = retVal.replaceAll(" ", bs + " ");
    return retVal;
  }

  /**
   * This opens the log file. Since the log file is potentially the only communication with the
   * user, a detectable failure to open the log file is a fatal error and will stop execution.
   */
  public static void initLogOrExit() {
    logFileName =
        new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date())
            + "_"
            + MULTITOOL_NAME
            + ".log";
    try {
      logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFileName, true)));
      String tNow = formattedTimeNow();
      logWriter.println("Log opened at " + tNow);
    } catch (IOException e) {
      System.out.println("Can't open log file " + logFileName + " because " + e.getMessage());
      System.exit(0);
    }
  }

  public static String formattedTimeNow() {
    Calendar timestamp = new GregorianCalendar();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mmZ");
    return dateFormat.format(timestamp.getTime());
  }

  static VersionData getVersionDataFromFilename(String jarName, String toolName) {
    VersionData noMatch = new VersionData(-1, false);
    VersionData noResult = new VersionData(0, false);
    jarName = jarName.toLowerCase();
    toolName = toolName.toLowerCase();
    String dotJar = ".jar";
    if (!jarName.startsWith(toolName)) return noMatch;
    if (!jarName.endsWith(dotJar)) return noMatch;
    boolean mod = false;
    int i = jarName.indexOf(toolName);
    String hold = jarName.substring(i + toolName.length());
    if (!hold.startsWith("-")) return noResult;
    hold = hold.substring(1);
    i = hold.indexOf(".jar");
    hold = hold.substring(0, i);
    if (hold.contains("-m")) {
      i = hold.indexOf("-m");
      hold = hold.substring(0, i);
      mod = true;
    }
    if (!isAllDigits(hold)) return noResult;
    int verVal = Integer.parseInt(hold);
    return new VersionData(verVal, mod);
  }

  /**
   * Checks to determine whether a string is all digits or not.
   *
   * @param checkMe - String to be checked.
   * @return - true if input is exclusively digits
   */
  public static boolean isAllDigits(String checkMe) {
    if (checkMe == null) {
      return false;
    } else {
      return checkMe.chars().allMatch(Character::isDigit);
    }
  }
}
