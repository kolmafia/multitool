package us.kolmafia.multitool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ToolDataTest {
  private static ToolData td;

  @BeforeAll
  static void beforeAll() {
    td = new ToolData("xyzzy");
  }

  @Test
  public void itShouldHaveName() {
    assertEquals("xyzzy", td.getToolName());
  }
}
