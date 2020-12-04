package Controller;

import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    Controller controller = new Controller();
    while (true) {
      System.out.println("Options: ");
      System.out.println("standardStart, parseQueries, parseFiles, parseSites, parseStemming");
      System.out.println("elasticIndex, elasticIndex, post, teamCloud, index, merge, clear, crawl");
      System.out.println("queryElastic, query, dummyTest, print, eval [qrel, results], or quit");
      Scanner input = new Scanner(System.in);
      String command = input.nextLine();
      switch (command) {
        // runs parseQueries(), parseTestDocuments(), and parseStemming()
        case "standardStart":
          controller.standardStart();
          break;
        case "start":
          controller.standardStart();
          break;
        // reads from the document giving example queries
        case "parseQueries":
          controller.parseQueries();
          break;
        // creates an index connected to elastic search api
        case "elasticIndex":
          controller.createElasticIndex();
          break;
        // posts the parsed files to elastic api
        case "post":
          controller.postFiles();
          break;
        // connects to a different api; rather than going through localhost, goes through a cloud
        case "teamCloud":
          controller.teamCloud();
          break;
        // reads from the many example documents that give document IDs and their text content
        case "parseFiles":
          controller.parseTestDocuments();
          break;
        // reads from files with content from actual websites crawled by crawler
        case "parseSites":
          controller.parseWebsites();
          break;
        // fetches the document contents from elastic search
        // calculates scores from example queries
        case "queryElastic":
          controller.queryElastic();
          break;
        // fetches the document contents from inverted index files created and stored locally
        // calculates scores from example queries
        case "query":
          controller.queryPrivate();
          break;
        // reads from the file containing info used for "stemming" words to their root forms
        case "parseStemming":
          controller.parseStemming();
          break;
        case "index":
          // takes parsed documents and generates inverted index writing it to local files
          controller.privateIndex();
          break;
        // the next step in creating an index; merges from many files to one file so usable
        case "merge":
          controller.merge();
          break;
        // a test that is run from command line, can be changed depending as needed
        // or to do one-time functionality
        case "dummyTest":
          controller.test();
          break;
        // deletes all the local inverted index files
        case "clear":
          controller.clear();
          break;
        // hands over to a crawling system
        // after this type the number of sites to crawl, or one of the other commands
        case "crawl":
          controller.crawl();
          break;
        // lets you enter document ids of documents you want to print to system.out at will
        case "print":
          controller.printFiles();
          break;
        // end program
        case "quit":
          System.out.println("quitting");
          return;
        // just a shorthand
        case "eval1":
          controller.evaluate("IR_Data/AP_DATA/qrels.adhoc.51-100.AP89.txt",
                  "out/RankingResults/es.txt");
          break;
        // evaluation function that takes arguments
        // for evaluating results of queryElastic or queryPrivate() for correctness of results
        // averaged across queries and documents
        default:
          if (command.contains(" ")) {
            String[] split = command.split(" +");
            if (split[0].equals("eval")) {
              if (split.length < 3) {
                controller.evaluate();
              } else {
                controller.evaluate(split[1], split[2]);
              }
            } else {
              System.out.println("command contains a space");
            }
          } else {
            System.out.println("not a command");
          }
      }
    }
  }
}
