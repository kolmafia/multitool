package com.github.multitool;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ToolData {
  private int currentVersion;
  private int latestVersion;
  private final String toolName;
  private Path downloadURL;
  private List<String> localJars = new ArrayList<>();

  public ToolData(String name) {
    this.toolName = name;
  }

  public int getLatestVersion() {
    return latestVersion;
  }

  public void setLatestVersion(int latestVersion) {
    this.latestVersion = latestVersion;
  }

  public int getCurrentVersion() {
    return currentVersion;
  }

  public void setCurrentVersion(int currentVersion) {
    this.currentVersion = currentVersion;
  }

  public List<String> getLocalJars() {
    return localJars;
  }

  public void setLocalJars(List<String> localJars) {
    this.localJars = localJars;
  }

  public String getToolName() {
    return toolName;
  }

  public Path getDownloadURL() {
    return downloadURL;
  }

  public void setDownloadURL(Path downloadURL) {
    this.downloadURL = downloadURL;
  }

  @Override
  public String toString() {
    String NL = "\n";
    return "ToolData for " + toolName + NL +
            "currentVersion=" + currentVersion + NL +
            "latestVersion=" + latestVersion + NL +
            "downloadURL=" + downloadURL + NL +
            "localJars=" + localJars;
  }
}
