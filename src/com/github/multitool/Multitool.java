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
  public static void main(String[] args) throws IOException {
    processMultitool();
    processKolMafia();
    processJava();
    try {
      startSecondJVM();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    System.exit(0);
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


  private static void processMultitool() {

    List<String> tools = processDirectory("multitool");
    System.out.println("Local multitool jar files.");
    System.out.println(tools);
    String latest = getMultitoolRelease();
    System.out.println("Latest multitool release: " + latest);
    System.out.println("End multitool");
  }

  private static void processKolMafia() {
    List<String> tools = processDirectory("KoLmafia");
    System.out.println("Local KoLmafia jar files.");
    System.out.println(tools);
    String latest = getMafiaRelease();
    System.out.println("latest KoLmafia release: " + latest);
    System.out.println("End KoLmafia");
  }

  private static List<String> processDirectory(String nameRoot) {
    // Returns a list of file names in the current directory that match
    List<String> retVal = new ArrayList<>();
    String lcRoot = nameRoot.toLowerCase();
    String currentWorkingDir = Paths.get("").toAbsolutePath().toString();
    try {
      File f = new File(currentWorkingDir);
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

  private static String getMafiaRelease() {
    String rel = "https://api.github.com/repos/kolmafia/kolmafia/releases/latest";
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
      throw new RuntimeException(e);
    }
    int ptr;
    StringBuilder buffer = new StringBuilder();
    while (true) {
      try {
        if ((ptr = is.read()) == -1) break;
      } catch (IOException e) {
        throw new RuntimeException(e);
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
    return retVal;
  }

  private static String getMultitoolRelease() {

    String rel = "https://api.github.com/repos/kolmafia/multitool/releases/latest";
    String retVal;
    URL url;
    try {
      url = new URL(rel);
    } catch (MalformedURLException e) {
      System.out.println(e);
      retVal = "Unknown";
      return retVal;
    }
    InputStream is;
    try {
      is = url.openStream();
    } catch (IOException e) {
      retVal = "Unknown";
      return retVal;
    }
    int ptr;
    StringBuilder buffer = new StringBuilder();
    while (true) {
      try {
        if ((ptr = is.read()) == -1) break;
      } catch (IOException e) {
        System.out.println(e);
        retVal = "Unknown";
        return retVal;
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
    return retVal;
  }

  private static void processJava() {
    String javaVersion = getJavaVersion();
    System.out.println("Local, running Java version: " + javaVersion);
  }

  private static String getJavaVersion() {
    new StringBuilder("Unknown");
    StringBuilder retVal;
    char[] pp = System.getProperty("java.home").toCharArray();
    int end = pp.length - 1;
    boolean first;
    first = false;
    retVal = new StringBuilder();
    for (int x = end; x >= 0; x--) {
      char ppp = pp[x];
      if (Character.isDigit(ppp)) {
        first = true;
        retVal.insert(0, ppp);
      } else {
        if (first) break;
      }
    }
    return retVal.toString();
  }
}
