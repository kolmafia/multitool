package us.kolmafia.multitool;

import java.io.File;

public class Constants {
  /**
   * The code setting ROOT_LOCATION was lifted from KoLConstants.java and the dependency on
   * UtilityConstants.java eliminated by copying code from there.
   */
  public static final File BASE_LOCATION =
      new File(System.getProperty("user.dir")).getAbsoluteFile();

  public static final File HOME_LOCATION =
      new File(System.getProperty("user.home")).getAbsoluteFile();
  public static final boolean USE_OSX_STYLE_DIRECTORIES =
      System.getProperty("os.name").startsWith("Mac");
  public static final boolean USE_LINUX_STYLE_DIRECTORIES =
      USE_OSX_STYLE_DIRECTORIES && !System.getProperty("os.name").startsWith("Win");
  public static final File ROOT_LOCATION =
      Boolean.getBoolean("useCWDasROOT")
          ? BASE_LOCATION
          : USE_OSX_STYLE_DIRECTORIES
              ? new File(HOME_LOCATION, "Library/Application Support/KoLmafia")
              : USE_LINUX_STYLE_DIRECTORIES ? new File(HOME_LOCATION, ".kolmafia") : BASE_LOCATION;
}
