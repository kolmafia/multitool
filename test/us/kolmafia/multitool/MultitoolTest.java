package us.kolmafia.multitool;

import static java.nio.file.Files.copy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static us.kolmafia.multitool.Multitool.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

  private boolean validateDestination(Path dest) {
    File dFile = dest.toFile();
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
  public void itShouldFilesThatWerePutThere() {
    Path source = new File(ROOT_LOCATION.toString() + "/FileResources").toPath();
    List<String> inDir = processDirectory("Kolmafia");
    assertTrue(inDir.isEmpty());
    Path dest = ROOT_LOCATION.toPath();
    assertTrue(validateDestination(dest));
    try {
      copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      fail("Could not copy files");
    }
    inDir = processDirectory("Kolmafia");
    assertEquals(2, inDir.size());
    assertTrue(inDir.contains("KolMafia-1066.jar"));
    assertTrue(inDir.contains("KolMafia-1066-M.jar"));
    assertFalse(inDir.contains("KolMafia-Latest.jar"));
    // dlete copied
  }
}
