package us.kolmafia.multitool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Multitool {

  private static String cwd;
  private static String localJava;
  private static int localJavaVersion;
  private static PrintWriter logWriter;

  public static void main(String[] args) {
    String logFileName =
        new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()) + "_multitool.log";
    try {
      logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFileName)));
    } catch (IOException e) {
      System.out.println("Can't open log file " + logFileName + " because " + e.getMessage());
      System.exit(0);
    }
    processLocalInformation();
    int preferredJava = getPreferredJava();
    ToolData multiData = processTool("multitool");
    ToolData mafiaData = processTool("kolmafia");
    if (multiData.isNeedToDownload()) {
      downloadAFile(multiData.getDownloadURL());
      logWriter.println("***");
      logWriter.println("Downloaded newer version of multitool.");
      logWriter.println("***");
      multiData = processTool("multitool");
    }
    if (mafiaData.isNeedToDownload()) {
      downloadAFile(mafiaData.getDownloadURL());
      logWriter.println("***");
      logWriter.println("Downloaded newer version of KoLmafia.");
      logWriter.println("***");
      mafiaData = processTool("kolmafia");
    }
    displayLocalInformation();
    logWriter.println("Preferred Java version: " + preferredJava);
    if (preferredJava > localJavaVersion) {
      logWriter.println("Local Java too low for KoLmafia.  Running disabled.");
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

  private static void processLocalInformation() {
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
    return Integer.parseInt(retVal);
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
      int i = jarName.indexOf(toolName);
      String hold = jarName.substring(i + toolName.length() + 1);
      i = hold.indexOf(".jar");
      hold = hold.substring(0, i);
      if (hold.contains("-m")) {
        i = hold.indexOf("-m");
        hold = hold.substring(0, i);
        retVal.setLocalModificationFound(true);
      }
      runMe = systemJarName;
      localVersion = Integer.parseInt(hold);
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

  private static int getLocalJavaVersion() {
    String locStr = System.getProperty("java.version");
    int i = locStr.indexOf(".");
    String num = locStr.substring(0, i);
    return Integer.parseInt(num);
  }

  private static int getLatestReleaseVersion(String tool) {
    String rel = "https://api.github.com/repos/kolmafia/" + tool + "/releases/latest";
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
    String dq = "\"";
    String js = buffer.toString();
    String findMe = dq + "name" + dq + ":";
    int i = js.indexOf(findMe);
    js = js.substring(i + findMe.length());
    i = js.indexOf(",");
    js = js.substring(0, i);
    js = js.replaceAll("\"", "");
    retVal = js;
    return Integer.parseInt(retVal);
  }

  private static List<String> processDirectory(String nameRoot) {
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

  private static String cleanPath(String path) {
    String retVal = path;
    String fs = "/";
    String bs = "\\\\";
    retVal = retVal.replaceAll(bs, fs);
    retVal = retVal.replaceAll(" ", bs + " ");
    return retVal;
  }
}
