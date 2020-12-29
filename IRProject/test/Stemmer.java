import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Indexing.PrivateIndexing;
import Indexing.TermPosition;
import Parsers.ParseStopwords;
import Parsers.StemmerParser;
import Util.Paths;

import static org.junit.Assert.assertEquals;

public class Stemmer {

  private String stopWordsFile;
  private HashSet<String> stopwords;
  private PrivateIndexing indexing;

  public Stemmer() {
    stopWordsFile = Paths.stoplist;
    try {
      ParseStopwords sw = new ParseStopwords();
      stopwords = sw.parseFile(stopWordsFile);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    indexing = new PrivateIndexing(stopwords, null);
  }

  @Test
  public void tokenizer() {

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

  @Test
  public void tokenizer2() {
    String text = " \"content\" : \"\"\"   Going to a shopping mall to buy a robot\n" +
            "may sound like a futuristic fantasy, but Jean Du Teau is gambling\n" +
            "in the present on science fiction becoming fact.\n" +
            "   Du Teau owns Robot World, a retail robot store tucked into a\n" +
            "Rochester shopping center next to a maternity boutique and an\n" +
            "optician's shop.\n" +
            "   Visitors are greeted at the door by RB5X, a small, gray figure\n" +
            "with a shiny domed head who chirps ``Hello stranger.''\n" +
            "   Du Teau, France-born president of Rochester Robotics Inc.,\n" +
            "opened Robot World in June to meet what he sees as a need for\n" +
            "robots for the general public.\n" +
            "   While Rochester Robotics makes robots for nuclear power plant\n" +
            "inspection and modifies service robots for business use, Robot\n" +
            "World sells mostly educational and hobby models to schools and\n" +
            "computer enthusiasts. But Du Teau says it won't be long before most\n" +
            "of his sales are to people buying personal robots for their homes.\n" +
            "   ``Robots are today where computers were 10 years ago,'' he said.\n" +
            "``Most people perceive that the robotic age is going to happen in\n" +
            "the year 2000. The robotic age is here.''\n" +
            "   The store's products range from RoboDuck, a $29 beginner's robot\n" +
            "for children, to RB5X, which costs $2,700 and is Robot World's\n" +
            "biggest seller to schools.\n" +
            "   Cost is the main thing standing between most Americans and their\n" +
            "own personal robots, Du Teau said. He expects prices to begin\n" +
            "dropping as the number of new products grows. That, he said, is\n" +
            "happening already.\n" +
            "   Not everyone in the robot business is quite as optimistic.\n" +
            "Household robots are coming, other experts say, but they don't all\n" +
            "agree on when.\n" +
            "   ``We're still quite a ways from a robot in every house,'' said\n" +
            "Jeff Burnstein, managing director of National Service Robot\n" +
            "Association, an industry group based in Ann Arbor, Mich.\n" +
            "   Burnstein said robot technology is still developing. There just\n" +
            "aren't many home robots ready to be marketed that can do anything\n" +
            "useful, he said.\n" +
            "   ``People think that there's a market out there for home robots,\n" +
            "but there just aren't products.''\n" +
            "   Dozens, maybe hundreds of researchers are working on robots,\n" +
            "Burnstein said. He believes the main obstacle to getting them on\n" +
            "the market is the lack of a major backer for robot products.\n" +
            "   Burnstein thinks the robot revolution will happen sometime in\n" +
            "the next decade.\n" +
            "   ``I don't think we're talking about 30 to 50 years away,'' he\n" +
            "said. ``It's just a matter of when there's going to be a big\n" +
            "breakthrough.''\n" +
            "   Dana Ballard, a computer science professor and robotics\n" +
            "researcher at the University of Rochester, sees the breakthrough as\n" +
            "still further off, perhaps 20 to 50 years away.\n" +
            "   Recent advances in technology have sped up robotics research,\n" +
            "Ballard said. Many problems need to be solved before home robots\n" +
            "can be made viable.\n" +
            "   ``I don't think these things are going to be translated into\n" +
            "marketable items in the near future,'' he said. ``Putting the whole\n" +
            "thing together is going to take a while.''\n" +
            "   Like Du Teau and Burnstein, however, Ballard believes that there\n" +
            "will someday be a robot in every house.\n" +
            "   ``I think there'll be a whole spectrum of machines that have\n" +
            "various sorts of motor capabilities,'' he said. ``It's hard to\n" +
            "envision the form of these things because they haven't been thought\n" +
            "of yet.''\n" +
            "   Home robots are unlikely to be all-purpose human-like creatures\n" +
            "such as the lovable R2D2 character in Star Wars. A simple _ for\n" +
            "humans _ task like getting a drink out of the refrigerator involves\n" +
            "a whole series of extremely complicated procedures for a robot, Du\n" +
            "Teau said.\n" +
            "   ``To have a robot that does everything _ we're a long way from\n" +
            "that,'' he said. ``If we had a robot that would clean house we\n" +
            "could retire by now.''\n" +
            "   More likely are robots that perform specific functions _ such as\n" +
            "dishwashing, pool cleaning or security.\n" +
            "   Security robots that roam the house, detect any usual movement\n" +
            "or sounds and call the police, will be among the first home robots\n" +
            "on the market in the next few months, Du Teau said.\n" +
            "   Other robots that Du Teau says will be in the store by January\n" +
            "include ``The Lawn Ranger,''a computerized self-propelled lawn\n" +
            "mower, and Newton, a robot that can do multiple duty as a security\n" +
            "guard, answering machine, alarm clock and language teacher for\n" +
            "children.\n" +
            "   ``It's somewhat of a gimmick, but it's a nice gimmick because it\n" +
            "has purpose,'' Du Teau said.\n" +
            "   Purpose is what separates the new breed of robots from the smart\n" +
            "but generally useless robots that made a brief foray onto the\n" +
            "market in the early 1980s, Burnstein said.\n" +
            "   At least six home robot products that will be on the market in\n" +
            "the next few months, with more to follow next summer, Du Teau said.\n" +
            "And while other entrepreneurs scramble to get in on the boom he's\n" +
            "sure is coming, Du Teau is already there _ and already planning\n" +
            "more branches.\n" +
            "   ``To me the most important thing is we're the first,'' he said.\n" +
            "``We are pioneers.''";
    System.out.println("number of characters: " + text.toCharArray().length);
    ArrayList<TermPosition> tokens = indexing.tokenize(text, 0, true);
    System.out.println("number of terms: " + tokens.size());
    System.out.println(tokens.toString());
    assertEquals(442, tokens.size());
  }
}
