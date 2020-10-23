package HW2;

import org.javatuples.Triplet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Tokenizer {

  private HashMap<Integer, String> docHashes;
  private HashMap<Integer, String> tokens;
  private ArrayList<Triplet> index;

  public ArrayList<Triplet> index(HashMap<String, String> documents, HashSet<String> stopwords,
                       HashMap<String, String> wordSubstitutions) {
    // ID -> document mapping
    docHashes = new HashMap<Integer, String>();
    for (String docId : documents.keySet()) {
      docHashes.put(docId.hashCode(), docId);
    }
    // ID -> token mapping
    tokens = new HashMap<Integer, String>();
    // Array of triplets: (termId, documentId, position)
    index = new ArrayList<Triplet>();
    for (String docId : documents.keySet()) {
      int tokenCnt = 0;
      for (String token : tokenizeString(documents.get(docId))) {
        if (stopwords.contains(token)) {
          continue;
        }
        String stemmed = wordSubstitutions.get(token);
        tokenCnt++;
        Integer tokenHash = stemmed.hashCode();
        if (!tokens.containsKey(tokenHash)) {
          tokens.put(tokenHash, stemmed);
        }
        index.add(Triplet.with(tokenHash, docId.hashCode(), tokenCnt));
      }
    }
    return index;
  }

  // todo:
  // doing these processes seperately might be slow, maybe use library to speed up
  public String[] tokenizeString(String s) {
    return s.toLowerCase().replaceAll("[^\\w\\s]", "").split("\\s");
  }

  private HashMap<Integer, String> getDocHashes() {
    return docHashes;
  }

  private HashMap<Integer, String> getTokens() {
    return tokens;
  }

  private ArrayList<Triplet> getIndex() {
    return index;
  }
}
