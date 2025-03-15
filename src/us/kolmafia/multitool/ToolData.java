package us.kolmafia.multitool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ToolData {
  private final String toolName;
  private int currentVersion;
  private int latestVersion;
  private String downloadURL;
  private File latestJarFile;
  private List<String> localJars = new ArrayList<>();
  private boolean localModificationFound;
  private boolean needToDownload;

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

  public String getDownloadURL() {
    return downloadURL;
  }

  public void setDownloadURL(String downloadURL) {
    this.downloadURL = downloadURL;
  }

  @Override
  public String toString() {
    String NL = "\n";
    return "ToolData for "
        + toolName
        + NL
        + "currentVersion="
        + currentVersion
        + NL
        + "latestVersion="
        + latestVersion
        + NL
        + "downloadURL="
        + downloadURL
        + NL
        + "needToDownload="
        + needToDownload
        + NL
        + "localJars="
        + localJars
        + NL
        + "latestJarFile="
        + latestJarFile
        + NL
        + "localModificationFound="
        + localModificationFound;
  }

  public boolean isLocalModificationFound() {
    return localModificationFound;
  }

  public void setLocalModificationFound(boolean localModificationFound) {
    this.localModificationFound = localModificationFound;
  }

  public File getLatestJarFile() {
    return latestJarFile;
  }

  public void setLatestJarFile(File latestJar) {
    this.latestJarFile = latestJar;
  }

  public boolean isNeedToDownload() {
    return needToDownload;
  }

  public void setNeedToDownload(boolean needToDownload) {
    this.needToDownload = needToDownload;
  }
}
