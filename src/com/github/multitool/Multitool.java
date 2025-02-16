package com.github.multitool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Multitool {

  private static String cwd;
  private static String localJava;
  private static int localJavaVersion;

  public static void main(String[] args) {
    processLocalInformation();
    displayLocalInformation();
    int preferredJava = getPreferredJava();
    System.out.println("Preferred Java version: " + preferredJava + "\n");
    ToolData multiData = processTool("multitool");
    displayToolInformation(multiData);
    ToolData mafiaData = processTool("kolmafia");
    displayToolInformation(mafiaData);
    if (multiData.isNeedToDownload()) {
      downloadAFile(multiData.getDownloadURL());
    }
    if (mafiaData.isNeedToDownload()) {
      downloadAFile(mafiaData.getDownloadURL());
    }
    if (args.length > 0) {
      if (args[0].equalsIgnoreCase("run")) {
        try {
          startSecondJVM(mafiaData);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    System.exit(0);
  }

  private static void processLocalInformation() {
    String separator = FileSystems.getDefault().getSeparator();
    cwd = cleanPath(Paths.get("").toAbsolutePath().toString());
    localJava = cleanPath(System.getProperty("java.home") + separator + "bin" + separator + "java");
    localJavaVersion = getLocalJavaVersion();
  }

  public static void displayLocalInformation() {
    System.out.println("Current working directory: " + cwd);
    System.out.println("Path to local Java: " + localJava);
    System.out.println("Local Java version: " + localJavaVersion);
  }

  private static int getPreferredJava() {
    // I can't actually figure out where to get this from GitHub or KoLmafia.
    // Deferred for the moment.
    return 21;
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
    System.out.println(tool + "\n");
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
    System.out.println(command);
    Runtime.getRuntime().exec(command);
  }

  private static int getLocalJavaVersion() {
    new StringBuilder("Unknown");
    StringBuilder local;
    char[] pp = System.getProperty("java.home").toCharArray();
    int end = pp.length - 1;
    boolean first;
    first = false;
    local = new StringBuilder();
    for (int x = end; x >= 0; x--) {
      char ppp = pp[x];
      if (Character.isDigit(ppp)) {
        first = true;
        local.insert(0, ppp);
      } else {
        if (first) break;
      }
    }
    return Integer.parseInt(local.toString());
  }

  private static int getLatestReleaseVersion(String tool) {
    String rel = "https://api.github.com/repos/kolmafia/" + tool + "/releases/latest";
    String retVal;
    URL url;
    try {
      url = new URL(rel);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    InputStream is;
    try {
      is = url.openStream();
    } catch (IOException e) {
      System.out.println(e);
      return 0;
    }
    int ptr;
    StringBuilder buffer = new StringBuilder();
    while (true) {
      try {
        if ((ptr = is.read()) == -1) break;
      } catch (IOException e) {
        System.out.println(e);
        return 0;
      }
      buffer.append((char) ptr);
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
            retVal.add(file);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
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
