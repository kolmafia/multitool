package com.github.multitool;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Multitool {
  public static void main(String[] args) {
    processMultitool();
    processKolMafia();
    // processJava();
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
    List<String> tools = processDirectory("Multitool");
    System.out.println(tools);
    System.out.println("End Multitool");
  }

  private static void processKolMafia() {
    List<String> tools = processDirectory("KoLmafia");
    System.out.println(tools);
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
}
