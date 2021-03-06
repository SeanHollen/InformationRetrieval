package Indexing;

import org.tartarus.snowball.ext.EnglishStemmer;
import java.io.*;
import java.util.*;
import Util.FileUtil;

public class PrivateIndexing {

  // used
  private HashMap<String, String> wordSubstitutions;
  private HashSet<String> stopwords;
  private HashMap<String, String> documents;
  // mappings
  private HashMap<String, Integer> docHashes;
  private HashMap<Integer, String> tokensHash;
  // derived
  private int numTokens;
  private double avgDocLength;
  private HashMap<Integer, Integer> docLengthsMap;

  // used in merging
  private ArrayList<File> catalogs;
  private ArrayList<File> invLists;
  private File cat1;
  private BufferedReader cat1Reader;
  private File cat2;
  private BufferedReader cat2Reader;
  private File invList1;
  private BufferedReader invList1Reader;
  private File invList2;
  private BufferedReader invList2Reader;
  private File newCatalog;
  private File newInvIndex;
  private FileWriter catWriter;
  private FileWriter indexWriter;

  public PrivateIndexing() {
    // Token Hash -> Token
    tokensHash = new HashMap<>();
    // Document Hash -> Document
    docHashes = new HashMap<>();
    // Document ID -> Document text
    documents = new HashMap<>();
    // Document Hash -> Document Length
    docLengthsMap = new HashMap<>();
  }

  public PrivateIndexing(HashSet<String> stopwords, HashMap<String, String> wordSubstitutions) {
    this();
    this.stopwords = stopwords;
    this.wordSubstitutions = wordSubstitutions;
  }

  public void putDocuments(HashMap<String, String> documents) {
    int i = 0;
    for (String docId : documents.keySet()) {
      i++;
      docHashes.put(docId, i);
    }
    this.documents.putAll(documents);
  }

  public void putDocument(String docId, String text) {
    docHashes.put(docId, docHashes.size() + 1);
    this.documents.put(docId, text);
  }

  public void index(String outDir) {
    Object[] docKeys = documents.keySet().toArray();
    ArrayList<TermPosition> newTokens = new ArrayList<>();
    HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> newSortedTokens;
    numTokens = 0;
    System.out.println("indexing " + docKeys.length + " documents");
    for (int i = 0; i < docKeys.length; i++) {
      String key = (String) docKeys[i];
      int docHash = docHashes.get(key);
      ArrayList<TermPosition> newTPs = tokenize(documents.get(key), docHash, true);
      numTokens += newTPs.size();
      docLengthsMap.put(docHash, newTPs.size());
      newTokens.addAll(newTPs);
      boolean areEnoughTokensForOneFile = i != docKeys.length - 1 && !(i % 1000 == 0 && i != 0);
      if (areEnoughTokensForOneFile) {
        continue;
      }
      System.out.println("Creating new catalog and inverted index file, " + i + " files indexed");
      System.out.println("File name: " + (int) Math.ceil((double) i / 1000) + ".txt");
      newSortedTokens = toSortedForm(newTokens);
      newTokens = new ArrayList<>();
      try {
        setupIndexFiles(outDir, i);
        writeTokensToFile(newSortedTokens);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    putDataToFiles(outDir);
  }

  private void setupIndexFiles(String outDir, int i) throws IOException {
    String catPath = outDir + "/catalogs/" + (int) Math.ceil((double) i / 1000) + ".txt";
    String invPath = outDir + "/invList/" + (int) Math.ceil((double) i / 1000) + ".txt";
    File catalog = new File(catPath);
    File invIndex = new File(invPath);
    boolean created = catalog.createNewFile() && invIndex.createNewFile();
    catWriter = new FileWriter(catPath);
    indexWriter = new FileWriter(invPath);
  }

  private void writeTokensToFile(HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>
                                               newSortedTokens) throws IOException {
    int bitPlaceStart = 0;
    for (int tokenHash : newSortedTokens.keySet()) {
      HashMap<Integer, ArrayList<Integer>> tokens = newSortedTokens.get(tokenHash);
      StringBuilder tokensBuilder = new StringBuilder();
      for (int doc : tokens.keySet()) {
        ArrayList<Integer> tokenDocs = tokens.get(doc);
        StringBuilder positionsBuilder = new StringBuilder();
        for (int pos : tokenDocs) {
          positionsBuilder.append(pos);
          positionsBuilder.append(",");
        }
        tokensBuilder.append(doc + "|" + tokenDocs.size() + "|" + positionsBuilder.toString() + ";");
      }
      String termString = tokenHash + "=" + tokensBuilder.toString() + "\n";
      indexWriter.write(termString);
      String catContent = tokenHash + " " + tokensHash.get(tokenHash) + " ";
      int bitPlaceEnd = bitPlaceStart + termString.getBytes().length;
      catWriter.write(catContent + bitPlaceStart + " " + bitPlaceEnd + "\n");
      bitPlaceStart = bitPlaceEnd;
    }
    catWriter.close();
    indexWriter.close();
  }

  private void putDataToFiles(String outDir) {
    avgDocLength = (double) numTokens / (double) docHashes.size();
    System.out.println("Vocabulary size: " + tokensHash.size());
    System.out.println("Number of docs: " + documents.size());
    System.out.println("Aggregate number of Tokens: " + numTokens);
    System.out.println("Average Number of Tokens: " + avgDocLength);
    System.out.println("Doc Lengths map: " + docLengthsMap);
    putToFile(outDir + "/docIds.txt", docHashes);
    putToFile(outDir + "/tokenIds.txt", tokensHash);
    putToFile(outDir + "/docLengths.txt", docLengthsMap);
    putToFile(outDir + "/aggInfo.txt", new Integer[]
            {tokensHash.size(), documents.size(), numTokens, (int) avgDocLength});
  }

  private void putToFile(String location, Object toPut) {
    try {
      File file = new File(location);
      if (file.createNewFile()) {
        System.out.println("created a file: " + location);
      } else {
        System.out.println("failed to create a file, likely already exists, and will write over");
      }
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
      oos.writeObject(toPut);
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void merge(String dir, boolean testMode) {
    boolean isRunDiscontinuous = tokensHash == null || tokensHash.size() == 0;
    if (isRunDiscontinuous) {
      loadTokenHashes(dir);
    }
    setCatalogAndInvLists(dir);
    int length = invLists.size();
    while (catalogs.size() > 1 && invLists.size() > 1) {
      int l = catalogs.size() / 2;
      for (int i = 0; i < l; i++) {
        try {
          setReaders(catalogs, invLists);
          createkNewFiles(++length, dir);
          writeNewFiles();
          if (testMode) {
            System.out.println("returned");
            return;
          }
          updateListsOfFiles();
          deleteUsedFiles();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void loadTokenHashes(String dir) {
    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dir + "/docIds.txt"));
      tokensHash = (HashMap<Integer, String>) ois.readObject();
      System.out.println("tokens size: " + tokensHash.size());
      ois.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setCatalogAndInvLists(String dir) {
    File[] catalogsArr = new File(dir + "/catalogs").listFiles(new FileUtil());
    File[] invListsArr = new File(dir + "/invList").listFiles(new FileUtil());
    if (catalogsArr == null || invListsArr == null) {
      throw new IllegalArgumentException("failure to find files in dir");
    }
    catalogs = new ArrayList<>(Arrays.asList(catalogsArr));
    Collections.sort(catalogs, new FileUtil());
    invLists = new ArrayList<>(Arrays.asList(invListsArr));
    Collections.sort(invLists, new FileUtil());
    System.out.println("inv lists: " + invLists);
  }

  private void setReaders(ArrayList<File> catalogs, ArrayList<File> invLists)
          throws FileNotFoundException{
    cat1 = catalogs.get(0);
    cat1Reader = new BufferedReader(new FileReader(cat1));
    cat2 = catalogs.get(1);
    cat2Reader = new BufferedReader(new FileReader(cat2));
    invList1 = invLists.get(0);
    invList1Reader = new BufferedReader(new FileReader(invList1));
    invList2 = invLists.get(1);
    invList2Reader = new BufferedReader(new FileReader(invList2));
    System.out.println("merging files " + invList1.getName() + " and " + invList2.getName());
  }

  private void createkNewFiles(int length, String dir) throws IOException {
    String catPath = dir + "/catalogs/" + length + ".txt";
    String invPath = dir + "/invList/" + length + ".txt";
    newCatalog = new File(catPath);
    newInvIndex = new File(invPath);
    if (newCatalog.createNewFile() && newInvIndex.createNewFile()) {
      System.out.println("2 files created: " + length + ".txt");
    } else {
      System.out.println("At least one file not created (perhaps already exists)");
    }
    catWriter = new FileWriter(catPath);
    indexWriter = new FileWriter(invPath);
  }

  private void writeNewFiles() throws IOException {
    String line;
    ArrayList<String[]> cat1Arr = new ArrayList<>();
    ArrayList<String[]> cat2Arr = new ArrayList<>();
    while ((line = cat1Reader.readLine()) != null) {
      cat1Arr.add(line.split(" ", 2));
    }
    while ((line = cat2Reader.readLine()) != null) {
      cat2Arr.add(line.split(" ", 2));
    }
    int bytePlaceStart = 0;
    int bytePlaceEnd = 0;
    int place1 = 0;
    int place2 = 0;
    String newTerm;
    String oldTerm = "";
    String newIndexLine;
    while (place1 < cat1Arr.size() && place2 < cat2Arr.size()) {
      if (toGetNewTermFromCat1(place1, place2, cat1Arr, cat2Arr)) {
        newTerm = cat1Arr.get(place1)[0];
        newIndexLine = invList1Reader.readLine();
        place1++;
      } else {
        newTerm = cat2Arr.get(place2)[0];
        newIndexLine = invList2Reader.readLine();
        place2++;
      }
      if (newTerm.equals(oldTerm)) {
        String toWrite = newIndexLine.split("=", 2)[1];
        indexWriter.write(toWrite);
        bytePlaceEnd += toWrite.getBytes().length;
      } else {
        if (bytePlaceEnd != 0) {
          catWriter.write(oldTerm + " " + tokensHash.get(Integer.parseInt(oldTerm))
                  + " " + bytePlaceStart + " " + bytePlaceEnd + "\n");
          indexWriter.write("\n");
          bytePlaceEnd++;
        }
        bytePlaceStart = bytePlaceEnd;
        indexWriter.write(newIndexLine);
        bytePlaceEnd += newIndexLine.getBytes().length;
      }
      oldTerm = newTerm;
    }
    catWriter.write(oldTerm + " " + tokensHash.get(Integer.parseInt(oldTerm))
            + " " + bytePlaceStart + " " + bytePlaceEnd);

  }

  private boolean toGetNewTermFromCat1(int place1, int place2, ArrayList<String[]> cat1Arr,
                                  ArrayList<String[]> cat2Arr) {
    double evaluation;
    if (place1 == cat1Arr.size()) {
      evaluation = 1;
    } else if (place2 == cat2Arr.size()) {
      evaluation = -1;
    } else {
      evaluation = cat1Arr.get(place1)[1].compareTo(cat2Arr.get(place2)[1]);
    }
    return evaluation <= 0;
  }

  private void updateListsOfFiles() throws IOException {
    catalogs.add(newCatalog);
    invLists.add(newInvIndex);
    catalogs.remove(cat1);
    catalogs.remove(cat2);
    invLists.remove(invList1);
    invLists.remove(invList2);
    catWriter.close();
    indexWriter.close();
  }

  private void deleteUsedFiles() {
    if (!(cat1.delete() && cat2.delete() && invList1.delete() && invList2.delete())) {
      System.out.println("failed to wipe one of the old files");
    }
  }

  // Uses In-Disk stemmer
  public ArrayList<TermPosition> tokenizeDeprecated(String document, int from, boolean doStemming) {
    String[] terms = document.toLowerCase()
            .replaceAll("[^\\w\\s]", "").split("\\s");
    ArrayList<TermPosition> tokensList = new ArrayList<>();
    int place = 0;
    for (String token : terms) {
      if (token.equals("")) continue;
      if (stopwords.contains(token)) continue;
      place++;
      if (doStemming && wordSubstitutions.containsKey(token)) {
        token = wordSubstitutions.get(token);
      }
      Integer tokenHash = token.hashCode();
      tokensHash.putIfAbsent(tokenHash, token);
      tokensList.add(new TermPosition(tokenHash, tokensHash.get(tokenHash), from, place));
    }
    return tokensList;
  }

  // Uses Library stemmer (PorterStemmer), not in-disk stemmer
  public ArrayList<TermPosition> tokenize(String document, int from, boolean doStemming) {
    EnglishStemmer stemmer = new EnglishStemmer();
    String[] spaceSplit = document.toLowerCase().split("\\s+");
    ArrayList<TermPosition> tokensList = new ArrayList<>();
    int place = 0;
    for (String next : spaceSplit) {
      String[] punctSplit = next.split("[^\\w'.]");
      for (String term : punctSplit) {
        term = term.replaceAll("[^\\w]", "");
        if (term.equals("") || term.equals("0")) {
          continue;
        }
        if (doStemming) {
          stemmer.setCurrent(term);
          stemmer.stem();
          term = stemmer.getCurrent();
        }
        // stopwords
        if (stopwords.contains(term)) {
          continue;
        }
        place++;
        Integer tokenHash = term.hashCode();
        tokensHash.putIfAbsent(tokenHash, term);
        tokensList.add(new TermPosition(tokenHash, tokensHash.get(tokenHash), from, place));
      }
    }
    return tokensList;
  }


  private LinkedHashMap<Integer, HashMap<Integer, ArrayList<Integer>>> toSortedForm(
          ArrayList<TermPosition> termPositions) {
    Collections.sort(termPositions);
    LinkedHashMap<Integer, HashMap<Integer, ArrayList<Integer>>> toReturn
            = new LinkedHashMap<>();
    for (TermPosition tp : termPositions) {
      if (!toReturn.containsKey(tp.getTermHash())) {
        toReturn.put(tp.getTermHash(), new HashMap<>());
      }
      if (!toReturn.get(tp.getTermHash()).containsKey(tp.getDocHash())) {
        toReturn.get(tp.getTermHash()).put(tp.getDocHash(), new ArrayList<Integer>());
      }
      toReturn.get(tp.getTermHash()).get(tp.getDocHash()).add(tp.getPosition());
    }
    return toReturn;
  }

  public void clear(String dir) {
    File[] catalogsArr = new File(dir + "/catalogs").listFiles();
    File[] invListsArr = new File(dir + "/invList").listFiles();
    if (catalogsArr == null || invListsArr == null) {
      throw new IllegalArgumentException("nulls");
    }
    for (File file : catalogsArr) {
      file.delete();
    }
    for (File file : invListsArr) {
      file.delete();
    }
  }

}
