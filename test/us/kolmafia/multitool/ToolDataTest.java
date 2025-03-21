package us.kolmafia.multitool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ToolDataTest {
  private static ToolData td;
  private static final String toolName = "ToolName";

  @BeforeAll
  static void beforeAll() {
    td = new ToolData(toolName);
  }

  @Test
  public void itShouldHaveName() {
    assertEquals(toolName, td.getToolName());
  }

  @Test
  public void itShouldPreserveCurrentVersionNumber() {
    int ver = 123;
    td.setCurrentVersion(ver);
    assertEquals(ver, td.getCurrentVersion());
  }

  @Test
  public void itShouldPreserveLatestVersionNumber() {
    int ver = 666;
    td.setLatestVersion(ver);
    assertEquals(ver, td.getLatestVersion());
  }

  @Test
  public void itShouldPreserveNeedToDownload() {
    boolean need = false;
    td.setNeedToDownload(need);
    assertEquals(need, td.isNeedToDownload());
  }

  @Test
  public void itShouldPreserveLocalModification() {
    boolean need = false;
    td.setLocalModificationFound(need);
    assertEquals(need, td.isLocalModificationFound());
  }

  @Test
  public void itShouldPreserveDownloadURL() {
    String fauxURL = "https://api.github.com/repos/kolmafia/" + toolName + "/releases/latest";
    td.setDownloadURL(fauxURL);
    assertEquals(fauxURL, td.getDownloadURL());
  }

  @Test
  public void itShouldPreserveLatestJarFile() {
    File fauxFile = new File("NoName");
    td.setLatestJarFile(fauxFile);
    assertEquals(fauxFile, td.getLatestJarFile());
  }

  @Test
  public void itShouldPreserveLocalJars() {
    List<String> local = new ArrayList<>();
    local.add("a");
    local.add("b");
    td.setLocalJars(local);
    assertEquals(local, td.getLocalJars());
  }

  @Test
  public void itShouldOverrideToString() {
    ToolData newTd = new ToolData(toolName);
    String expected =
        "ToolData for ToolName\n"
            + "currentVersion=0\n"
            + "latestVersion=0\n"
            + "downloadURL=null\n"
            + "needToDownload=false\n"
            + "localJars=[]\n"
            + "latestJarFile=null\n"
            + "localModificationFound=false";
    String got = newTd.toString();
    assertEquals(expected, got);
  }
}
