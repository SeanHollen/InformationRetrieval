import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Indexing.PrivateIndexing;
import Indexing.TermPosition;
import Parsers.ParseStopwords;
import Parsers.StemmerParser;

import static org.junit.Assert.assertEquals;

public class Stemmer {

  @Test
  public void stemming() {
    String stopWordsFile = "IR_Data/AP_DATA/stoplist.txt";
//    String stemmerFile = "IR_Data/AP_DATA/stem-classes.lst";
    HashSet<String> stopwords;
//    HashMap<String, String> stemwords;
    try {
      ParseStopwords sw = new ParseStopwords();
      stopwords = sw.parseFile(stopWordsFile);
//      StemmerParser stemmer = new StemmerParser();
//      stemwords = stemmer.parseFile(stemmerFile);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    PrivateIndexing indexing = new PrivateIndexing(stopwords, null);
    String text = "crystals. robotics, robo crystallised/crystal-laden crystallisation pooof " +
            "8.$ robot--camera,/ robotics-assembly";
    System.out.println(text);
    ArrayList<TermPosition> list = indexing.tokenize(text, 0, true);
    String expected = "[crystal(1047561014:0:1), robot(108685930:0:2), robo(3505994:0:3), " +
            "crystallis(657902496:0:4), crystal(1047561014:0:5), laden(102729336:0:6), " +
            "crystallis(657902496:0:7), pooof(106851367:0:8), 8(56:0:9), robot(108685930:0:10), " +
            "camera(-1367751899:0:11), robot(108685930:0:12), assembli(-373408298:0:13)]";
    assertEquals(expected, list.toString());
  }
}
