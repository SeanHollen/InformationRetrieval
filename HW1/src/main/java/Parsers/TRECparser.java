package Parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class TRECparser {

  // Map<DOCNO, TEXT>
  public HashMap<String, String> parseFiles(String dir) throws IOException {

    File directory = new File(dir);
    HashMap<String, String> map = new HashMap<String, String>();
    for (final File file : directory.listFiles()) {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line;
      StringBuilder text = new StringBuilder();
      String docNo = "";
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("<DOCNO>")) {
          docNo = line.substring("<DOCNO>".length(), line.length() - "</DOCNO>".length()).trim();
        }
        if (line.contains("<TEXT>")) {
          while (!(line = reader.readLine()).contains("</TEXT>")) {
            text.append(line);
            text.append("\n");
          }
          map.put(docNo, text.toString());
          text.delete(0, text.length());
        }

      }
    }

    return map;
  }

}
