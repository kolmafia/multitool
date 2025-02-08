package com.github.multitool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Multitool {

  private static String cdw;
  private static String localJava;
  private static int localJavaVersion;
  private static int preferredJava;

  public static void main(String[] args) {
    processLocalInformation();
    displayLocalInformation();
    preferredJava = getPreferredJava();
    System.out.println("Preferred Java version: " + preferredJava);
    ToolData multiData = processTool("multitool");
    displayToolInformation(multiData);
    ToolData mafiaData = processTool("kolmafia");
    displayToolInformation(mafiaData);
    try {
      startSecondJVM();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    System.exit(0);
  }

  private static void processLocalInformation() {
    String separator = FileSystems.getDefault().getSeparator();
    cdw = Paths.get("").toAbsolutePath().toString();
    localJava = System.getProperty("java.home") + separator + "bin" + separator + "java";
    localJavaVersion = getLocalJavaVersion();
  }

  public static void displayLocalInformation() {
    System.out.println("Current working directory: " + cdw);
    System.out.println("Path to local Java: " + localJava);
    System.out.println("Local Java version: " + localJavaVersion);
  }

  public static void startSecondJVM() throws Exception {
    String separator = FileSystems.getDefault().getSeparator();
    String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
    String dq = "\"";
    String jar =
        "C:/Users/frono/IdeaProjects/MultiTool/out/production/MultiTool/KolMafia-28320.jar";
    String command = dq + path + dq + " -jar " + jar;
    System.out.println(command);
    Runtime.getRuntime().exec(command);
  }

  private static List<String> processDirectory(String nameRoot) {
    // Returns a list of file names in the current directory that match
    List<String> retVal = new ArrayList<>();
    String lcRoot = nameRoot.toLowerCase();
    try {
      File f = new File(cdw);
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

  private static ToolData processTool(String toolName) {
    ToolData retVal = new ToolData(toolName);
    retVal.setLatestVersion(getLatestReleaseVersion(toolName));
    retVal.setLocalJars(processDirectory(toolName));
    retVal.setLocalModificationFound(false);
    List<String> locals = retVal.getLocalJars();
    int localVersion = 0;
    for (String jarName : locals) {
      int i = jarName.indexOf(toolName);
      String hold = jarName.substring(i + toolName.length() + 1);
      i = hold.indexOf(".jar");
      hold = hold.substring(0, i);
      if (hold.contains("-M")) {
        i = hold.indexOf("-M");
        hold = hold.substring(0, i);
        retVal.setLocalModificationFound(true);
      }
      localVersion = Integer.parseInt(hold);
    }
    retVal.setCurrentVersion(localVersion);
    return retVal;
  }

  private static void displayToolInformation(ToolData tool) {
    System.out.println(tool);
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

  private static int getPreferredJava() {
    int version = 21;
    // I can't actually figure out where to get this from GitHub or KoLmafia.
    // Deferred for the moment.
    return version;
  }
}
