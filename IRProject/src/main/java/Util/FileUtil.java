package Util;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

public class FileUtil implements Comparator<File>, FileFilter {

  public int compare(File a, File b) {
    return Integer.parseInt(a.getName().replaceAll("[^0-9]", ""))
            - Integer.parseInt((b.getName().replaceAll("[^0-9]", "")));
  }

  @Override
  public boolean accept(File file) {
    return !file.isHidden();
  }
}
