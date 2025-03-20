package us.kolmafia.multitool;

import static java.nio.file.Files.copy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static us.kolmafia.multitool.Constants.ROOT_LOCATION;
import static us.kolmafia.multitool.Multitool.cleanPath;
import static us.kolmafia.multitool.Multitool.cwd;
import static us.kolmafia.multitool.Multitool.processDirectory;
import static us.kolmafia.multitool.Multitool.processLocalInformation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    List<String> locals = processDirectory("KoLMafia");
    assertTrue(locals.isEmpty());
  }

  private boolean validateDestination(Path destination) {
    File dFile = destination.toFile();
    boolean retVal = dFile.exists();
    if (retVal) {
      retVal = dFile.isDirectory();
      if (retVal) {
        retVal = dFile.canWrite();
        if (retVal) {
          retVal = (Objects.requireNonNull(dFile.list()).length == 1);
        }
      }
    }
    return retVal;
  }

  @Test
  public void itShouldFindFilesThatWerePutThere() {
    Path source = new File(ROOT_LOCATION.toString() + "/FileResources").toPath();
    List<File> deleteWhenDone = new ArrayList<>();
    List<String> inDir = processDirectory("Kolmafia");
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
        throw new RuntimeException(e);
      }
    }
    inDir = processDirectory("Kolmafia");
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
    inDir = processDirectory("Kolmafia");
    assertTrue(inDir.isEmpty());
  }
}
