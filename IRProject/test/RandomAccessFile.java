import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;

public class RandomAccessFile {

  @Test
  public void test() {
    String fileText = "abcd\n" +
            "ABCD\n" +
            "1234\n" +
            "!@#%\n";
    String firstFew = "abcd\n" +
            "AB";
    int bitCount = firstFew.getBytes().length;
    File file = new File("aTestFile.txt");
    try {
      java.io.RandomAccessFile readFile = new java.io.RandomAccessFile(file, "r");
      byte[] bytes = new byte[10];
      readFile.seek(bitCount);
      readFile.read(bytes, 0, 10);
      //String s = sfb.intoString(bytes);
      String s = new String(bytes);
      System.out.println(s);
      assertEquals("CD\n1234\n!@", s);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void append() {
    try {
      File file = new File("out/CrawledDocuments/test.txt");
      PrintWriter contentWriter = new PrintWriter(new FileWriter(file, true));
      contentWriter.println("new line");
      contentWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
