package HW2;

import java.io.File;
import java.util.Comparator;

public class ByFileName implements Comparator<File> {

  public int compare(File a, File b) {
    return Integer.parseInt(a.getName().replaceAll("[^0-9]", ""))
            - Integer.parseInt((b.getName().replaceAll("[^0-9]", "")));
  }
}
