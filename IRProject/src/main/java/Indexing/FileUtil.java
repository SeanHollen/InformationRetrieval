package Indexing;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

public class FileUtil implements Comparator<File>, FileFilter {

  public int compare(File a, File b) {
    System.out.println(a.getName());
    System.out.println(a.getPath());
    System.out.println(b.getName());
    System.out.println(b.getPath());
    return Integer.parseInt(a.getName().replaceAll("[^0-9]", ""))
            - Integer.parseInt((b.getName().replaceAll("[^0-9]", "")));
  }

  @Override
  public boolean accept(File file) {
    return !file.isHidden();
  }
}
