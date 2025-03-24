package us.kolmafia.multitool;

import static java.nio.file.Files.copy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static us.kolmafia.multitool.Constants.KOLMAFIA_NAME;
import static us.kolmafia.multitool.Constants.MULTITOOL_NAME;
import static us.kolmafia.multitool.Constants.ROOT_LOCATION;
import static us.kolmafia.multitool.Multitool.cleanPath;
import static us.kolmafia.multitool.Multitool.cwd;
import static us.kolmafia.multitool.Multitool.getVersionDataFromFilename;
import static us.kolmafia.multitool.Multitool.getVersionFromInputStream;
import static us.kolmafia.multitool.Multitool.initLogOrExit;
import static us.kolmafia.multitool.Multitool.logFileName;
import static us.kolmafia.multitool.Multitool.logWriter;
import static us.kolmafia.multitool.Multitool.processDirectory;
import static us.kolmafia.multitool.Multitool.processLocalInformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MultitoolTest {

  @BeforeAll
  static void beforeAll() {
    processLocalInformation();
  }

  @Test
  public void itShouldBeRunningInRoot() {
    String cp = cleanPath(String.valueOf(ROOT_LOCATION.toPath()));
    assertEquals(cp, cwd);
  }

  @Test
  public void itShouldFindNoFiles() {
    List<String> locals = processDirectory(KOLMAFIA_NAME);
    assertTrue(locals.isEmpty());
  }

  private boolean validateDestination(Path destination) {
    File dFile = destination.toFile();
    return dFile.exists() && dFile.isDirectory() && dFile.canWrite();
  }

  @Test
  public void itShouldFindFilesThatWerePutThere() {
    initLogOrExit();
    Path source = new File(ROOT_LOCATION.toString() + "/FileResources").toPath();
    List<File> deleteWhenDone = new ArrayList<>();
    List<String> inDir = processDirectory(KOLMAFIA_NAME);
    assertTrue(inDir.isEmpty());
    Path destination = ROOT_LOCATION.toPath();
    assertTrue(validateDestination(destination));
    String[] files = source.toFile().list();
    assertNotNull(files);
    for (String file : files) {
      File sFile = new File(source + "/" + file);
      Path sPath = sFile.toPath();
      File dFile = new File(destination + "/" + file);
      deleteWhenDone.add(dFile);
      Path dPath = dFile.toPath();
      try {
        copy(sPath, dPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        fail(e.getMessage());
      }
    }
    inDir = processDirectory(KOLMAFIA_NAME);
    assertEquals(2, inDir.size());
    assertTrue(inDir.contains("KolMafia-1066.jar"));
    assertTrue(inDir.contains("KolMafia-1066-M.jar"));
    assertFalse(inDir.contains("KolMafia-Latest.jar"));
    for (File f : deleteWhenDone) {
      boolean whoCares = f.delete();
      if (!whoCares) {
        System.out.println("Failed to delete " + f);
      }
    }
    inDir = processDirectory(KOLMAFIA_NAME);
    assertTrue(inDir.isEmpty());
    cleanUpLog();
  }

  public void cleanUpLog() {
    logWriter.flush();
    logWriter.close();
    boolean whoCares = new File(logFileName).delete();
    if (!whoCares) {
      System.out.println("Failed to delete " + logFileName);
    }
  }

  @Test
  public void itShouldGetMultitoolVersion() {
    String name = ROOT_LOCATION.toString() + "/FileResources/latest" + MULTITOOL_NAME + ".json";
    FileInputStream fs = null;
    try {
      fs = new FileInputStream(name);
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    assertEquals(63, getVersionFromInputStream(fs));
  }

  @Test
  public void itShouldGetKoLmafiaVersion() {
    String name = ROOT_LOCATION.toString() + "/FileResources/latest" + KOLMAFIA_NAME + ".json";
    FileInputStream fs = null;
    try {
      fs = new FileInputStream(name);
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    assertEquals(28465, getVersionFromInputStream(fs));
  }

  @ParameterizedTest
  @CsvSource({
    "kolmafia, kolmafia, 0, false",
    "kolmafia-123, kolmafia, 0, false",
    "kolmafia123.jar, kolmafia, 0, false",
    "kolmafia-123.jar, kolmafia, 123, false",
    "kolmafia-123-m.jar, kolmafia, 123, true",
    "kolmafia-123-.jar, kolmafia, 0, false",
    "kolmafia-latest.jar, kolmafia, 0, false",
    "notatool-123.jar, kolmafia, 0, false"
  })
  public void itShouldGetVersions(
      String jarName, String toolName, int expectedVersion, boolean expectedmod) {
    VersionData vd = getVersionDataFromFilename(jarName, toolName);
    assertEquals(expectedVersion, vd.getVersion());
    assertEquals(expectedmod, vd.isModified());
  }
}
