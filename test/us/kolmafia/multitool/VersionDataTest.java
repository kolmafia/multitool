package us.kolmafia.multitool;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class VersionDataTest {
  @Test
  public void itShouldGetWhatWasSet() {
    VersionData vd = new VersionData(1066, true);
    assertEquals(1066, vd.getVersion());
    assertTrue(vd.isModified());
  }
}
