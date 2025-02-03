package com.github.multitool;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public abstract class StaticEntityLite {
  // Version information for the current version of KoLmafia.

  private static final String PRODUCT_NAME = "Multitool";
  private static Integer cachedRevisionNumber = null;
  private static String cachedVersionName = null;
  private static String cachedBuildInfo = null;
  private static Attributes cachedAttributes = null;

   public static Attributes getAttributes() {
    if (StaticEntityLite.cachedAttributes == null) {
      try {
        ClassLoader classLoader = StaticEntityLite.class.getClassLoader();
        if (classLoader != null) {
          for (Iterator<URL> it = classLoader.getResources("META-INF/MANIFEST.MF").asIterator();
              it.hasNext(); ) {
            Attributes attributes = new Manifest(it.next().openStream()).getMainAttributes();
            if (attributes != null
                && attributes.getValue("Main-Class") != null
                && attributes
                    .getValue("Main-Class")
                    .startsWith(StaticEntityLite.class.getPackageName())) {
              StaticEntityLite.cachedAttributes = attributes;
            }
          }
        }
      } catch (IOException e) {
      }
    }

    return StaticEntityLite.cachedAttributes;
  }

  public static String getVersion() {
    if (StaticEntityLite.cachedVersionName == null) {
      StringBuilder versionName =
          new StringBuilder(PRODUCT_NAME).append(" r").append(StaticEntityLite.getRevision());
      if (isCodeModified()) {
        versionName.append("-M");
      }
      StaticEntityLite.cachedVersionName = versionName.toString();
    }
    return StaticEntityLite.cachedVersionName;
  }

  private static boolean isCodeModified() {
    Attributes attributes = getAttributes();
    if (attributes == null) {
      return false;
    }

    return attributes.getValue("Build-Dirty").equals("true");
  }

  public static int getRevision() {
    if (StaticEntityLite.cachedRevisionNumber == null) {
      Attributes attributes = getAttributes();
      if (attributes != null) {
        String buildRevision = attributes.getValue("Build-Revision");

        if (buildRevision != null ) {
          try {
            StaticEntityLite.cachedRevisionNumber = Integer.parseInt(buildRevision);
          } catch (NumberFormatException e) {
            // fall through
          }
        }
      }

      if (StaticEntityLite.cachedRevisionNumber == null) {
        StaticEntityLite.cachedRevisionNumber = 0;
      }
    }

    return StaticEntityLite.cachedRevisionNumber;
  }

  public static String getBuildInfo() {
    if (StaticEntityLite.cachedBuildInfo == null) {
      StringBuilder cachedBuildInfo = new StringBuilder("Build");

      Attributes attributes = getAttributes();

      if (attributes != null) {
        String attribute = attributes.getValue("Build-Branch");
        if (attribute != null) {
          cachedBuildInfo.append(" ").append(attribute).append("-");
        }
        attribute = attributes.getValue("Build-Build");
        if (attribute != null) {
          cachedBuildInfo.append(attribute);
        }
        attribute = attributes.getValue("Build-Dirty");
        if (attribute.equals("true")) {
          cachedBuildInfo.append("-M");
        }
        attribute = attributes.getValue("Build-Jdk");
        if (attribute != null) {
          cachedBuildInfo.append(" ").append(attribute);
        }
        attribute = attributes.getValue("Build-OS");
        if (attribute != null) {
          cachedBuildInfo.append(" ").append(attribute);
        }
      }

      if (cachedBuildInfo.toString().equals("Build")) {
        cachedBuildInfo.append(" Unknown");
      }

      StaticEntityLite.cachedBuildInfo = cachedBuildInfo.toString();
    }

    return StaticEntityLite.cachedBuildInfo;
  }

  private static File getJDKWorkingDirectory() {
    File currentJavaHome = new File(System.getProperty("java.home"));

    if (StaticEntityLite.hasJDKBinaries(currentJavaHome)) {
      return currentJavaHome;
    }

    File javaInstallFolder = currentJavaHome.getParentFile();

    if (StaticEntityLite.hasJDKBinaries(javaInstallFolder)) {
      return javaInstallFolder;
    }

    return Arrays.stream(javaInstallFolder.listFiles())
        .filter(StaticEntityLite::hasJDKBinaries)
        .findAny()
        .orElse(null);
  }

  private static boolean hasJDKBinaries(File javaHome) {
    if (System.getProperty("os.name").startsWith("Windows")) {
      return new File(javaHome, "bin/javac.exe").exists();
    } else {
      return new File(javaHome, "bin/javac").exists();
    }
  }
}
