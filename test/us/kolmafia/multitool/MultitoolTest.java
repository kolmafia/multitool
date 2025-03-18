package us.kolmafia.multitool;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MultitoolTest {
  @Test
  public void itShouldBeRunningInRoot() {
    Multitool.processLocalInformation();
    String cp = Multitool.cleanPath(String.valueOf(Multitool.ROOT_LOCATION.toPath()));
    assertEquals(cp, Multitool.cwd);
  }
}
