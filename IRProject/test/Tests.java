import org.junit.Test;

import java.io.File;
import java.io.RandomAccessFile;
import static org.junit.Assert.assertEquals;
public class Tests {

  @Test
  public void testRandomAccessFile() {
    String fileText = "abcd\n" +
            "ABCD\n" +
            "1234\n" +
            "!@#%\n";
    String firstFew = "abcd\n" +
            "AB";
    int bitCount = firstFew.getBytes().length;
    File file = new File("aTestFile.txt");
    try {
      RandomAccessFile readFile = new RandomAccessFile(file, "r");
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

}
