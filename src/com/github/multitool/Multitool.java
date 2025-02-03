package com.github.multitool;

import java.nio.file.FileSystems;

public class Multitool {
  public static void main(String[] args) {
    String multitool = getNameAndVersion();
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
  private static String getNameAndVersion() {
    String retVal = "";
    retVal = StaticEntityLite.getVersion();
    return retVal;
  }
}
