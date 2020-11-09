package HW2;

public class StringFromBytes {

  // todo are you kidding me?
  public String intoString(byte[] bytes) {
    StringBuilder builder = new StringBuilder();
    for (byte b : bytes) {
      builder.append((char)b);
    }
    return builder.toString();
  }
}
